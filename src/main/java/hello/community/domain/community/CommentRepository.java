package hello.community.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
//JpaRepository는 Spring Data JPA에서 제공하는 인터페이스로, 기본적인 CRUD 및 페이징 작업을 위한 메서드를 포함하고 있습니다.

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByCommunity_id(Long communityId);
    //이 메서드는 특정 communityId에 속하는 모든 댓글을 조회합니다.
}
