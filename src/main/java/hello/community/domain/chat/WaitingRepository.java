package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findByGroupBuyId(Long groupBuyId);
    List<Waiting> findByGroupBuyIdAndAccepted(Long groupBuyId, boolean accepted);
}
