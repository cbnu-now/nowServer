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



}
