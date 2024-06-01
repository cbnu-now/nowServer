package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    // 사용자 ID로 UserChatRoom 목록을 찾는 메서드
    List<UserChatRoom> findByUserId(Long userId);

    // 사용자 ID와 채팅방 ID로 UserChatRoom을 찾는 메서드 추가
    UserChatRoom findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}
