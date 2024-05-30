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
}
