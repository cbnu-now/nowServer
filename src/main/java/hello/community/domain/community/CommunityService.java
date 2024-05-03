package hello.community.domain.community;

import hello.community.domain.groupBuy.GroupBuyDto;
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
public class CommunityService {
    private final UserRepository userRepository;
    private final LikedRepository likedRepository;
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;


    public void createCommunity(CommunityDto.CommunityInfo communityInfo, String url) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Community community = new Community();
        community.setPhoto(url);
        community.setTitle(communityInfo.getTitle());
        community.setContent(communityInfo.getContent());
        community.setCategory(communityInfo.getCategory());
        community.setLongitude(communityInfo.getLongitude());
        community.setLatitude(communityInfo.getLatitude());
        community.setAddress(communityInfo.getAddress());

        community.setUser(user);
        community.setCreatedAt(LocalDateTime.now());
        community.setLikes(0L);
        community.setView(0L);
        communityRepository.save(community);
    }

    public void createComment(Long communityId, String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티글이 존재하지 않습니다."));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setCommunity(community);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void updateComment(Long commentId, String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        // 댓글 작성자와 현재 로그인한 유저가 같은지 확인
        if (comment.getUser().getId() != userId) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        comment.setContent(content);
    }

    public void deleteComment(Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        // 댓글 작성자와 현재 로그인한 유저가 같은지 확인
        if (comment.getUser().getId() != userId) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }

    public CommunityDto.viewGroupBuyInfo getCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티글이 존재하지 않습니다."));
        Users user = userRepository.findById(community.getUser().getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        community.setView(community.getView() + 1);
        List<Comment> byIdCommunityId = commentRepository.findByCommunity_id(communityId);

        // comment를 commentComponent로 변환
        List<CommunityDto.CommentComponent> comments = byIdCommunityId.stream().map(comment -> CommunityDto.CommentComponent.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writerName(comment.getUser().getName())
                .writerImg(comment.getUser().getPhoto())
                .createdAt(comment.getCreatedAt())
                .build()).toList();

        return CommunityDto.viewGroupBuyInfo.builder()
                .writerName(user.getName())
                .writerImg(user.getPhoto())
                .address(community.getAddress())
                .title(community.getTitle())
                .img(community.getPhoto())
                .content(community.getContent())
                .Category(community.getCategory())
                .createdAt(community.getCreatedAt())
                .view(community.getView())
                .likes(community.getLikes())
                .comments(comments)
                .build();
    }

    public void likeCommunity(Long communityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);

        Community community = communityRepository.findById(communityId).orElseThrow(() -> new IllegalArgumentException("해당 모집글이 존재하지 않습니다."));
        Users user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Liked liked = likedRepository.findByUserIdAndCommunityId(userId, communityId);
        if (liked == null) {
            community.setLikes(community.getLikes() + 1);
            Liked liking = new Liked();
            liking.setCommunityId(community.getId());
            liking.setUserId(userId);
            likedRepository.save(liking);
        } else {
            community.setLikes(community.getLikes() - 1);
            likedRepository.delete(liked);
        }
    }


    public List<CommunityDto.viewCommunityListInfo> getCommunityByLocation(Double latitude, Double longitude) {
        List<Community> allCommunity = communityRepository.findAll();// 데이터베이스에서 모든 게시물 가져오기
        List<CommunityDto.viewCommunityListInfo> nearbyCommunity = new ArrayList<>();

        // 유저 찾기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);

        if (userId == null) {
            throw new IllegalArgumentException("User not found");
        }

        for (Community community : allCommunity) {
            double distance = calculateDistanceInMeter(latitude, longitude, community.getLatitude(), community.getLongitude());

            if (distance <= 1500) { // 1.5km 이내

                CommunityDto.viewCommunityListInfo viewGroupBuyListInfo = CommunityDto.viewCommunityListInfo.builder()
                        .title(community.getTitle())
                        .img(community.getPhoto())
                        .category(community.getCategory())
                        .createdAt(community.getCreatedAt())
                        .likes(community.getLikes())
                        .id(community.getId())
                        .content(community.getContent())
                        .address(community.getAddress())
                        .build();
                nearbyCommunity.add(viewGroupBuyListInfo);
            }
        }

        return nearbyCommunity;
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

    public void updateCommunity(CommunityDto.CommunityInfo communityInfo, String url, Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티글이 존재하지 않습니다."));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        if (community.getUser().getId() != userId) {
            throw new IllegalArgumentException("글 작성자만 수정할 수 있습니다.");
        }

        // null 이 아닌값들 업데이트
        if (communityInfo.getTitle() != null) {
            community.setTitle(communityInfo.getTitle());
        }
        if (communityInfo.getContent() != null) {
            community.setContent(communityInfo.getContent());
        }
        if (communityInfo.getCategory() != null) {
            community.setCategory(communityInfo.getCategory());
        }
        if (communityInfo.getLatitude() != null) {
            community.setLatitude(communityInfo.getLatitude());
        }
        if (communityInfo.getLongitude() != null) {
            community.setLongitude(communityInfo.getLongitude());
        }
        if (communityInfo.getAddress() != null) {
            community.setAddress(communityInfo.getAddress());
        }
        if (url != null) {
            community.setPhoto(url);
        }



    }

    public void deleteGroupBuy(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티글이 존재하지 않습니다."));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = Long.parseLong(username);
        if (community.getUser().getId() != userId) {
            throw new IllegalArgumentException("글 작성자만 삭제할 수 있습니다.");
        }
        communityRepository.delete(community);
    }
}
