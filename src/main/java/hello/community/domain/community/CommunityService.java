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
}
