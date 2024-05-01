package hello.community.domain.community;

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
}
