package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // 2번 API

public interface WaitingRepository extends JpaRepository<Waiting,Long> {
    List<Waiting> findByGroupBuyId(Long groupBuyId);    // 2번 API
    List<Waiting> findByUserId(Long userId); // 사용자 ID로 대기자 조회 // 2번 API
    List<Waiting> findByGroupBuyIdAndAccepted(Long groupBuyId, boolean accepted); // 3번 API

}
