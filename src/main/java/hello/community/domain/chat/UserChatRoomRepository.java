package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    List<UserChatRoom> findByUserId(Long userId);
}
