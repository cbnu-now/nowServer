package hello.community.domain.groupBuy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long>{
    List<GroupBuy> findByUserId(Long userId);
}
