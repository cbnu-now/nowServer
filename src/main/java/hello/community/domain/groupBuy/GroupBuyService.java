package hello.community.domain.groupBuy;

import hello.community.domain.liked.Liked;
import hello.community.domain.liked.LikedRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final LikedRepository likedRepository;

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
        groupBuy.setAddress(groupBuyInfo.getAddress());

        groupBuy.setUser(user);
        groupBuy.setCreatedAt(LocalDateTime.now());
        groupBuy.setCurrentCount(0L);
        groupBuy.setLikes(0L);
        groupBuy.setView(0L);

        groupBuyRepository.save(groupBuy);
    }

    // id로 모집글 조회
    public GroupBuyDto.viewGroupBuyInfo getGroupBuy(Long id) {
        GroupBuy groupBuy = groupBuyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));
        Users user = userRepository.findById(groupBuy.getUser().getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        groupBuy.setView(groupBuy.getView() + 1);

        GroupBuyDto.viewGroupBuyInfo viewGroupBuyInfo = GroupBuyDto.viewGroupBuyInfo.builder()
                .writerName(user.getName())
                .writerImg(user.getPhoto())
                .address(groupBuy.getAddress())
                .title(groupBuy.getTitle())
                .img(groupBuy.getPhoto())
                .content(groupBuy.getContent())
                .Category(groupBuy.getCategory())
                .createdAt(groupBuy.getCreatedAt())
                .headCount(groupBuy.getHeadCount())
                .currentCount(groupBuy.getCurrentCount())
                .view(groupBuy.getView())
                .likes(groupBuy.getLikes())
                .build();


        return viewGroupBuyInfo;

    }

    public List<GroupBuyDto.viewGroupBuyListInfo> getGroupBuyListByLocation(Double latitude, Double longitude) {
        List<GroupBuy> allGroupBuys = groupBuyRepository.findAll();// 데이터베이스에서 모든 게시물 가져오기
        List<GroupBuyDto.viewGroupBuyListInfo> nearbyGroupBuys = new ArrayList<>();

        // 유저 찾기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);

        if(userId == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<Liked> likedList = likedRepository.findByUserId(userId);
        Set<Long> likedGroupBuyIds = likedList.stream().map(Liked::getGroupBuyId).collect(Collectors.toSet());

        for (GroupBuy groupBuy : allGroupBuys) {
            double distance = calculateDistanceInMeter(latitude, longitude, groupBuy.getLatitude(), groupBuy.getLongitude());

            if (distance <= 300) { // 300m 이내
                boolean isLiked = likedGroupBuyIds.contains(groupBuy.getId());

                GroupBuyDto.viewGroupBuyListInfo viewGroupBuyListInfo = GroupBuyDto.viewGroupBuyListInfo.builder()
                        .title(groupBuy.getTitle())
                        .img(groupBuy.getPhoto())
                        .Category(groupBuy.getCategory())
                        .createdAt(groupBuy.getCreatedAt())
                        .headCount(groupBuy.getHeadCount())
                        .currentCount(groupBuy.getCurrentCount())
                        .likes(groupBuy.getLikes())
                        .isLiked(isLiked)
                        .build();
                nearbyGroupBuys.add(viewGroupBuyListInfo);
            }
        }

        return nearbyGroupBuys;
    }


    // 위도, 경도로 거리 계산
    public double calculateDistanceInMeter(double lat1, double lon1, double lat2, double lon2) {
        // 지구 반지름 (미터 단위)
        final int R = 6371 * 1000;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }
}
