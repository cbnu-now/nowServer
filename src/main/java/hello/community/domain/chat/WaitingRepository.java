package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findByGroupBuyId(Long groupBuyId);
    List<Waiting> findByGroupBuyIdAndAccepted(Long groupBuyId, boolean accepted);
    Optional<Waiting> findByGroupBuyIdAndUserId(Long groupBuyId, Long userId); // 손들기 중복 불가 메서드
    void deleteById(Long waitingId); // 손들기 거절을 위한 메서드 추가
}
