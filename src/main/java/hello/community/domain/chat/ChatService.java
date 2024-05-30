package hello.community.domain.chat;

import hello.community.domain.groupBuy.GroupBuy;
import hello.community.domain.groupBuy.GroupBuyRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;  //2번 APi 추가
import java.util.stream.Collectors;     //2번 APi 추가

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final WaitingRepository waitingRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;    //3번 APi 추가
    private final UserChatRoomRepository userChatRoomRepository; //3번 APi 추가

    public void raiseHand(Long groupBuyId) {
        GroupBuy findedGroupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (findedGroupBuy.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인이 작성한 모집글에는 참여할 수 없습니다.");
        }

        Waiting waiting = new Waiting();
        waiting.setUser(user);
        waiting.setGroupBuy(findedGroupBuy);
        waitingRepository.save(waiting);
    }

    // 2번 손들기 알림 목록 조회 메서드 추가
    public List<WaitingNotificationDto> getWaitingNotifications(Long userId) {
        List<Waiting> waitings = waitingRepository.findByUserId(userId);
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

    // 3번  API
    // 대기자 상태를 업데이트하고 필요 시 채팅방을 생성하는 메서드 추가
    public void acceptWaiting(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow(() -> new IllegalArgumentException("해당 대기자가 존재하지 않습니다."));
        GroupBuy groupBuy = waiting.getGroupBuy();

        waiting.setAccepted(true); // 상태를 1(true)로 변경
        waitingRepository.save(waiting);

        // 현재 수락된 대기자 수를 확인
        List<Waiting> acceptedWaitings = waitingRepository.findByGroupBuyIdAndAccepted(groupBuy.getId(), true);
        if (acceptedWaitings.size() >= groupBuy.getHeadCount()) {
            // 채팅방 생성
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setChatRoomName(groupBuy.getTitle());
            chatRoomRepository.save(chatRoom);

            // 채팅방에 사용자 추가
            for (Waiting acceptedWaiting : acceptedWaitings) {
                UserChatRoom userChatRoom = new UserChatRoom();
                userChatRoom.setUser(acceptedWaiting.getUser());
                userChatRoom.setChatRoom(chatRoom);
                userChatRoomRepository.save(userChatRoom);
            }
        }
    }
}
