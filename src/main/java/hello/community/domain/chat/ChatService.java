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
    public void raiseHand(Long groupBuyId, Long userId) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            GroupBuy findedGroupBuy = groupBuyRepository.findById(groupBuyId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

            if (findedGroupBuy.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("본인이 작성한 모집글에는 참여할 수 없습니다.");
            }

            // 중복 손들기 요청 방지
            Optional<Waiting> existingWaiting = waitingRepository.findByGroupBuyIdAndUserId(groupBuyId, userId);
            if (existingWaiting.isPresent()) {
                throw new IllegalStateException("이미 해당 모집글에 손들기 요청을 하였습니다.");
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
    public List<WaitingNotificationDto> getWaitingNotifications(Long userId) {
        try {
            List<GroupBuy> groupBuys = groupBuyRepository.findByUserId(userId);

            return groupBuys.stream().flatMap(groupBuy -> {
                List<Waiting> waitings = waitingRepository.findByGroupBuyId(groupBuy.getId());

                // accept가 false인 대기자만 조회
                waitings = waitings.stream().filter(waiting -> !waiting.isAccepted()).collect(Collectors.toList());

                return waitings.stream().map(waiting -> new WaitingNotificationDto(
                        waiting.getId(),
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

    // 손들기 수락 & 채팅방 생성 메서드 수정
    public void acceptWaiting(Long waitingId, Long userId) {
        try {
            Waiting waiting = waitingRepository.findById(waitingId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
            GroupBuy groupBuy = waiting.getGroupBuy();

            // 모집 완료 상태 체크
            if (groupBuy.isCompleted()) {
                throw new IllegalStateException("모집이 완료되어 손들기 수락을 할 수 없습니다.");
            }

            waiting.setAccepted(true);
            waitingRepository.save(waiting);

            groupBuy.setCurrentCount(groupBuy.getCurrentCount() + 1);
            groupBuyRepository.save(groupBuy);

            // 수락된 유저는 손들기 알림 목록에서 제거
            //waitingRepository.deleteById(waitingId);

            // 채팅방 생성 조건 확인 및 생성
            if (groupBuy.getCurrentCount() >= groupBuy.getHeadCount()) {
                createChatRoomForGroupBuy(groupBuy);
            }

        } catch (Exception e) {
            logger.error("Error accepting waiting", e);
            throw new RuntimeException("Error accepting waiting", e);
        }
    }

    // 채팅방 생성 메서드
    public void createChatRoomForGroupBuy(GroupBuy groupBuy) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setGroupBuy(groupBuy);
        chatRoomRepository.save(chatRoom);

        // 모집된 인원을 채팅방에 추가
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

    // 손들기 거절 메서드 추가
    public void rejectWaiting(Long waitingId, Long userId) {
        try {
            // 대기자 거절 로직
            Waiting waiting = waitingRepository.findById(waitingId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
            if (!waiting.getGroupBuy().getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("손들기를 거절할 권한이 없습니다.");
            }
            waitingRepository.delete(waiting);
        } catch (Exception e) {
            logger.error("Error rejecting waiting", e);
            throw new RuntimeException("Error rejecting waiting", e);
        }
    }

    // 채팅방 목록 액티비티 내에 채팅방 정보 목록이 배열로 조회
    // 채팅방 목록 조회 메서드 추가
    public List<ChatRoomListDto> getChatRoomList(Long userId) {
        try {
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
                        chatRoom.getId(),
                        participantImages,
                        groupBuy.getPhoto(),
                        groupBuy.getTitle(),
                        chatRoom.getUserChatRooms().size(),
                        lastChat != null ? lastChat.getContent() : "",
                        lastChat != null ? lastChat.getCreatedAt() : null,
                        userChatRoom.getUnreadMessageCount()
                );
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting chat room list", e);
            throw new RuntimeException("Error getting chat room list", e);
        }
    }

    // 채팅 메시지 전송
    public void sendMessage(Long chatRoomId, ChatMessageDto chatMessageDto, Long userId) {
        try {
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

            List<UserChatRoom> userChatRooms = userChatRoomRepository.findByChatRoomId(chatRoomId);
            for (UserChatRoom userChatRoom : userChatRooms) {
                if (!userChatRoom.getUser().getId().equals(userId)) {
                    userChatRoom.setUnreadMessageCount(userChatRoom.getUnreadMessageCount() + 1);
                }

                // 메시지 전송 시에는 lastReadTime을 업데이트하지 않음
                //userChatRoom.setLastReadTime(LocalDateTime.now());
                userChatRoomRepository.save(userChatRoom);
            }
        } catch (Exception e) {
            logger.error("Error sending message", e);
            throw new RuntimeException("Error sending message", e);
        }
    }


    // 채팅방 상세 기록 조회
    public List<ChatRecordDto> getChatRecords(Long chatRoomId, Long userId) {
        Users currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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

        // 마지막 메시지 읽음 시간 및 미읽은 메시지 카운트 초기화
        Optional<UserChatRoom> optionalUserChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        UserChatRoom userChatRoom = optionalUserChatRoom.orElseThrow(() -> new IllegalArgumentException("해당 채팅방에 사용자가 존재하지 않습니다."));
        userChatRoom.setLastReadTime(LocalDateTime.now());
        userChatRoom.setUnreadMessageCount(0); // 미읽은 메시지 카운트 초기화
        userChatRoomRepository.save(userChatRoom);

        return chatRecords;
    }

    // 채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        try {
            Optional<UserChatRoom> optionalUserChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            UserChatRoom userChatRoom = optionalUserChatRoom.orElseThrow(() -> new IllegalArgumentException("해당 채팅방에 사용자가 존재하지 않습니다."));

            userChatRoomRepository.delete(userChatRoom);
        } catch (Exception e) {
            logger.error("Error leaving chat room", e);
            throw new RuntimeException("Error leaving chat room", e);
        }
    }

    // 손들기 거절 메서드 추가 (이름 변경)
    public void declineWaiting(Long waitingId, Long userId) {
        try {
            // 대기자 거절 로직
            Waiting waiting = waitingRepository.findById(waitingId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
            if (!waiting.getGroupBuy().getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("손들기를 거절할 권한이 없습니다.");
            }
            waitingRepository.delete(waiting);
        } catch (Exception e) {
            logger.error("Error declining waiting", e);
            throw new RuntimeException("Error declining waiting", e);
        }
    }



}