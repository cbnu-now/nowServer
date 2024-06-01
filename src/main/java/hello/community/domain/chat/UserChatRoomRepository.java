package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    // 사용자 ID로 UserChatRoom 목록을 찾는 메서드 추가
    List<UserChatRoom> findByUserId(Long userId);
}
