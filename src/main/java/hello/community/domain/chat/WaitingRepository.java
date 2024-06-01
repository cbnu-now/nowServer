package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("SELECT w FROM Waiting w WHERE w.groupBuy.id = :groupBuyId")
    List<Waiting> findByGroupBuyId(@Param("groupBuyId") Long groupBuyId);
}
