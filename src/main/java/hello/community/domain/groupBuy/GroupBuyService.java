package hello.community.domain.groupBuy;

import hello.community.domain.chat.ChatService;
import hello.community.domain.liked.Liked;
import hello.community.domain.liked.LikedRepository;
import hello.community.domain.user.UserRepository;
import hello.community.domain.chat.ChatRoomRepository;
import hello.community.domain.chat.UserChatRoomRepository;
import hello.community.domain.chat.WaitingRepository;
import hello.community.domain.chat.ChatRoom;
import hello.community.domain.chat.Waiting;
import hello.community.domain.chat.UserChatRoom;
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
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final WaitingRepository waitingRepository;
    private final ChatService chatService; // ChatService 주입

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
        groupBuy.setSpotName(groupBuyInfo.getSpotName());

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


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId2 = Long.parseLong(username);
        Liked liked = likedRepository.findByUserIdAndGroupBuyId(userId2, id);

        boolean isLiked;
        if (liked == null) {
            isLiked = false;
        } else {
            isLiked = true;
        }

        // 모집 완료 여부 체크
        groupBuy.checkAndSetCompleted();
        groupBuyRepository.save(groupBuy); // 상태 변경을 저장

        GroupBuyDto.viewGroupBuyInfo viewGroupBuyInfo = GroupBuyDto.viewGroupBuyInfo.builder()
                .latitude(groupBuy.getLatitude())
                .longitude(groupBuy.getLongitude())
                .spotName(groupBuy.getSpotName())
                .writerName(user.getName())
                .writerImg(user.getPhoto())
                .address(groupBuy.getAddress())
                .title(groupBuy.getTitle())
                .img(groupBuy.getPhoto())
                .content(groupBuy.getContent())
                .isWriter(groupBuy.getUser().getId().equals(userId2))
                .isLiked(isLiked)
                .Category(groupBuy.getCategory())
                .createdAt(groupBuy.getCreatedAt())
                .headCount(groupBuy.getHeadCount())
                .currentCount(groupBuy.getCurrentCount())
                .view(groupBuy.getView())
                .likes(groupBuy.getLikes())
                .isCompleted(groupBuy.isCompleted()) // 모집 완료 여부 추가
                .build();


        return viewGroupBuyInfo;

    }

    // 조기 마감 메서드 수정
    public void closeEarly(Long groupBuyId) {
        GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        groupBuy.closeEarly();
        groupBuyRepository.save(groupBuy);

        // ChatService를 사용하여 채팅방을 생성합니다.
        chatService.createChatRoomForGroupBuy(groupBuy);
    }

    public List<GroupBuyDto.viewGroupBuyListInfo> getGroupBuyListByLocation(Double latitude, Double longitude,Long distance2) {
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

            if (distance <= distance2) { // 300m 이내
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
                        .id(groupBuy.getId())
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

    public void likeGroupBuy(Long groupBuyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);

        GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Liked liked = likedRepository.findByUserIdAndGroupBuyId(userId, groupBuyId);
        if (liked == null) {
            groupBuy.setLikes(groupBuy.getLikes() + 1);
            Liked liking = new Liked();
            liking.setGroupBuyId(groupBuy.getId());
            liking.setUserId(userId);
            likedRepository.save(liking);
        } else {
            groupBuy.setLikes(groupBuy.getLikes() - 1);
            likedRepository.delete(liked);
        }
    }

    public void updateGroupBuy(GroupBuyDto.GroupBuyInfo groupBuyInfo, String url, Long groupBuyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        // 작성자와 수정하려는 사람이 같은지 확인
        if (groupBuy.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // null 이 아닌것들만 업데이트
        if (groupBuyInfo.getTitle() != null) {
            groupBuy.setTitle(groupBuyInfo.getTitle());
        }
        if (groupBuyInfo.getContent() != null) {
            groupBuy.setContent(groupBuyInfo.getContent());
        }
        if (groupBuyInfo.getCategory() != null) {
            groupBuy.setCategory(groupBuyInfo.getCategory());
        }
        if (groupBuyInfo.getLatitude() != null) {
            groupBuy.setLatitude(groupBuyInfo.getLatitude());
        }
        if (groupBuyInfo.getLongitude() != null) {
            groupBuy.setLongitude(groupBuyInfo.getLongitude());
        }
        if (groupBuyInfo.getAddress() != null) {
            groupBuy.setAddress(groupBuyInfo.getAddress());
        }
        if (groupBuyInfo.getSpotName() != null) {
            groupBuy.setSpotName(groupBuyInfo.getSpotName());
        }
        if (groupBuyInfo.getHeadCount() != null) {
            groupBuy.setHeadCount(groupBuyInfo.getHeadCount());
        }
        if (url != null) {
            groupBuy.setPhoto(url);

        }
    }

    public void deleteGroupBuy(Long groupBuyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));

        // 작성자와 수정하려는 사람이 같은지 확인
        if (groupBuy.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        groupBuyRepository.delete(groupBuy);
    }
}