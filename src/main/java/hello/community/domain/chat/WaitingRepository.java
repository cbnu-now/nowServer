package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingRepository extends JpaRepository<Waiting,Long> {
}
