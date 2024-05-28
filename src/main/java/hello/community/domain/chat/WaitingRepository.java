package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    // 특정 모집글에 대한 모든 대기자를 반환하는 메서드
    List<Waiting> findByGroupBuyId(Long groupBuyId);
    List<Waiting> findByGroupBuyIdAndAccepted(Long groupBuyId, boolean accepted);
}
