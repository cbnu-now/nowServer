package hello.community.domain.community;

import hello.community.domain.groupBuy.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByUserId(Long userId);
}

//findByUserId 메서드는 특정 사용자의 ID로 커뮤니티 글을 조회하는 메서드입니다.
// Spring Data JPA는 메서드 이름을 분석하여 적절한 JPQL (Java Persistence Query Language) 쿼리를 생성합니다.