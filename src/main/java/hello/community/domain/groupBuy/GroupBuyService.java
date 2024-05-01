package hello.community.domain.groupBuy;

import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    public void createGroupBuy(GroupBuyDto.GroupBuyInfo groupBuyInfo, String url) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GroupBuy groupBuy = new GroupBuy();
        groupBuy.setPhoto(url);
        groupBuy.setTitle(groupBuyInfo.getTitle());
        groupBuy.setContent(groupBuyInfo.getContent());
        groupBuy.setCategory(groupBuyInfo.getCategory());
        groupBuy.setLongitude(groupBuyInfo.getLongitude());
        groupBuy.setLatitude(groupBuyInfo.getLatitude());
        groupBuy.setHeadCount(groupBuyInfo.getHeadCount());

        groupBuy.setUser(user);
        groupBuy.setCreatedAt(LocalDateTime.now());
        groupBuy.setCurrentCount(0L);
        groupBuy.setLikes(0L);
        groupBuy.setView(0L);

        groupBuyRepository.save(groupBuy);
    }
}
