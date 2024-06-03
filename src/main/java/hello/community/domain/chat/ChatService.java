package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.groupBuy.GroupBuyRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hello.community.global.jwt.JwtService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final WaitingRepository waitingRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRepository chatRepository;
    private final JwtService jwtService;

    // 모집글 상세 액티비티 하단의 손들기 버튼 클릭 시 대기자 테이블에 요청자에 대한 튜플 추가
    public void raiseHand(Long groupBuyId, String token) {
        try {
            Long userId = jwtService.getUserId(token);
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            GroupBuy findedGroupBuy = groupBuyRepository.findById(groupBuyId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

            if (findedGroupBuy.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("본인이 작성한 모집글에는 참여할 수 없습니다.");
            }

            Waiting waiting = new Waiting();
            waiting.setUser(user);
            waiting.setGroupBuy(findedGroupBuy);
            waitingRepository.save(waiting);
        } catch (Exception e) {
            logger.error("Error raising hand", e);
            throw new RuntimeException("Error raising hand", e);
        }
    }

    // 알림 목록 액티비티 내에서 손들기를 요청한 사용자의 정보를 배열로 조회
    public List<WaitingNotificationDto> getWaitingNotifications(String token) {
        try {
            Long userId = jwtService.getUserId(token);

            List<GroupBuy> groupBuys = groupBuyRepository.findByUserId(userId);

            return groupBuys.stream().flatMap(groupBuy -> {
                List<Waiting> waitings = waitingRepository.findByGroupBuyId(groupBuy.getId());
                return waitings.stream().map(waiting -> new WaitingNotificationDto(
                        waiting.getUser().getName(),
                        waiting.getUser().getPhoto(),
                        groupBuy.getTitle(),
                        groupBuy.getPhoto(),
                        waiting.getCreatedAt(),
                        groupBuy.getHeadCount(),
                        groupBuy.getCurrentCount()
                ));
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting waiting notifications", e);
            throw new RuntimeException("Error getting waiting notifications", e);
        }
    }

    // 손들기 수락 & 채팅방 생성
    public void acceptWaiting(Long waitingId, String token) {
        try {
            Long userId = jwtService.getUserId(token);

            // 대기자 수락 로직
            Waiting waiting = waitingRepository.findById(waitingId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
            GroupBuy groupBuy = waiting.getGroupBuy();

            waiting.setAccepted(true); // 상태 변경
            waitingRepository.save(waiting);

            // 현재 인원 수 업데이트
            groupBuy.setCurrentCount(groupBuy.getCurrentCount() + 1);
            groupBuyRepository.save(groupBuy);

            // 채팅방 생성 조건 확인 및 생성
            if (groupBuy.getCurrentCount() == groupBuy.getHeadCount()) {
                ChatRoom chatRoom = new ChatRoom();
                chatRoom.setGroupBuy(groupBuy);
                chatRoomRepository.save(chatRoom);

                // 채팅방에 사용자 추가
                List<Waiting> acceptedWaitings = waitingRepository.findByGroupBuyIdAndAccepted(groupBuy.getId(), true);
                for (Waiting acceptedWaiting : acceptedWaitings) {
                    UserChatRoom userChatRoom = new UserChatRoom();
                    userChatRoom.setUser(acceptedWaiting.getUser());
                    userChatRoom.setChatRoom(chatRoom);
                    userChatRoomRepository.save(userChatRoom);
                }

                // 파티장도 채팅방에 추가
                UserChatRoom ownerChatRoom = new UserChatRoom();
                ownerChatRoom.setUser(groupBuy.getUser());
                ownerChatRoom.setChatRoom(chatRoom);
                userChatRoomRepository.save(ownerChatRoom);
            }
        } catch (Exception e) {
            logger.error("Error accepting waiting", e);
            throw new RuntimeException("Error accepting waiting", e);
        }
    }

    // 채팅방 목록 액티비티 내에 채팅방 정보 목록이 배열로 조회
    // 채팅방 목록 조회 메서드 추가
    public List<ChatRoomListDto> getChatRoomList(String token) {
        try {
            Long userId = jwtService.getUserId(token);
            List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(userId);

            return userChatRooms.stream().map(userChatRoom -> {
                ChatRoom chatRoom = userChatRoom.getChatRoom();
                List<String> participantImages = chatRoom.getUserChatRooms().stream()
                        .map(uc -> uc.getUser().getPhoto())
                        .collect(Collectors.toList());
                GroupBuy groupBuy = chatRoom.getGroupBuy();
                Chat lastChat = chatRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom);
                long unreadCount = chatRepository.countByChatRoomAndCreatedAtAfter(chatRoom, userChatRoom.getLastReadTime());

                return new ChatRoomListDto(
                        participantImages,
                        groupBuy.getPhoto(),
                        groupBuy.getTitle(),
                        chatRoom.getUserChatRooms().size(),
                        lastChat != null ? lastChat.getContent() : "",
                        lastChat != null ? lastChat.getCreatedAt() : null,
                        (int) unreadCount
                );
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting chat room list", e);
            throw new RuntimeException("Error getting chat room list", e);
        }
    }

    // 채팅 메시지 전송
    public void sendMessage(Long chatRoomId, ChatMessageDto chatMessageDto, String token) {
        try {
            Long userId = jwtService.getUserId(token);
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

            Chat chat = new Chat();
            chat.setContent(chatMessageDto.getContent());
            chat.setCreatedAt(LocalDateTime.now());
            chat.setUser(user);
            chat.setChatRoom(chatRoom);
            chatRepository.save(chat);

            Optional<UserChatRoom> optionalUserChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            UserChatRoom userChatRoom = optionalUserChatRoom.orElseThrow(() -> new IllegalArgumentException("해당 채팅방에 사용자가 존재하지 않습니다."));
            userChatRoom.setLastReadTime(LocalDateTime.now());
            userChatRoomRepository.save(userChatRoom);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            throw new RuntimeException("Error sending message", e);
        }
    }

    // 채팅방 상세 기록 조회
    public List<ChatRecordDto> getChatRecords(Long chatRoomId, String token) {
        Long userId;
        try {
            userId = jwtService.getUserId(token);
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 토큰입니다.", e);
        }

        Users currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // chatRoomId가 null인 경우 예외 처리
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID가 null입니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        List<Chat> chats = chatRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        List<ChatRecordDto> chatRecords = new ArrayList<>();
        Long previousUserId = null;
        String chatRoomTitle = chatRoom.getGroupBuy().getTitle(); // 채팅방 제목 가져오기

        for (Chat chat : chats) {
            boolean isContinuousMessage = previousUserId != null && previousUserId.equals(chat.getUser().getId());
            boolean isMyMessage = chat.getUser().getId().equals(currentUser.getId());

            chatRecords.add(new ChatRecordDto(
                    chat.getUser().getPhoto(),
                    chat.getUser().getName(),
                    chat.getContent(),
                    chat.getCreatedAt(),
                    isContinuousMessage,
                    isMyMessage,
                    chatRoomTitle // 채팅방 제목 추가
            ));

            previousUserId = chat.getUser().getId();
        }

        return chatRecords;
    }

    // 채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long chatRoomId, String token) {
        try {
            Long userId = jwtService.getUserId(token);
            Optional<UserChatRoom> optionalUserChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            UserChatRoom userChatRoom = optionalUserChatRoom.orElseThrow(() -> new IllegalArgumentException("해당 채팅방에 사용자가 존재하지 않습니다."));

            userChatRoomRepository.delete(userChatRoom);
        } catch (Exception e) {
            logger.error("Error leaving chat room", e);
            throw new RuntimeException("Error leaving chat room", e);
        }
    }

}
