package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    // 사용자 ID로 UserChatRoom 목록을 찾는 메서드
    List<UserChatRoom> findByUserId(Long userId);

    // 사용자 ID와 채팅방 ID로 UserChatRoom을 찾는 메서드 추가
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    //안 읽은 메시지 추가를 위함.
    List<UserChatRoom> findByChatRoomId(Long chatRoomId);
}
