package hello.community.domain.community;

import hello.community.domain.groupBuy.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findByUserId(Long userId);
}
