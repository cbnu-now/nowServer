package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.groupBuy.GroupBuyRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    // 모집글 상세 액티비티 하단의 손들기 버튼 클릭 시 대기자 테이블에 요청자에 대한 튜플 추가
    public void raiseHand(Long groupBuyId) {
        GroupBuy findedGroupBuy = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (findedGroupBuy.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 모집글에는 참여할 수 없습니다.");
        }

        Waiting waiting = new Waiting();
        waiting.setUser(user);
        waiting.setGroupBuy(findedGroupBuy);
        waitingRepository.save(waiting);
    }

    // 알림 목록 액티비티 내에서 손들기를 요청한 사용자의 정보를 배열로 조회
    public List<WaitingNotificationDto> getWaitingNotifications(Long groupBuyId) {
        List<Waiting> waitings = waitingRepository.findByGroupBuyId(groupBuyId);
        return waitings.stream().map(waiting -> {
            GroupBuy groupBuy = waiting.getGroupBuy();
            return new WaitingNotificationDto(
                    waiting.getUser().getName(),
                    waiting.getUser().getPhoto(),
                    groupBuy.getTitle(),
                    groupBuy.getPhoto(),
                    waiting.getCreatedAt(),
                    groupBuy.getHeadCount(),
                    groupBuy.getCurrentCount()
            );
        }).collect(Collectors.toList());
    }

    // 손들기 수락 & 채팅방 생성
    public void acceptWaiting(Long waitingId) {
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
    }

    // 채팅방 목록 액티비티 내에 채팅방 정보 목록이 배열로 조회
    // 채팅방 목록 조회 메서드 추가
    // 채팅방 목록 조회 메서드 추가
    public List<ChatRoomListDto> getChatRoomList(Long userId) {
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
                    participantImages, // 모든 참여자의 이미지 URL 리스트
                    groupBuy.getPhoto(),
                    groupBuy.getTitle(),
                    chatRoom.getUserChatRooms().size(),
                    lastChat != null ? lastChat.getContent() : "",
                    lastChat != null ? lastChat.getCreatedAt() : null,
                    (int) unreadCount
            );
        }).collect(Collectors.toList());
    }


}
