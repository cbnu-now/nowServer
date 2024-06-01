package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 채팅방 내 가장 최근의 메시지를 찾는 메서드
    Chat findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 채팅방 내 특정 시간 이후의 메시지 수를 세는 메서드
    long countByChatRoomAndCreatedAtAfter(ChatRoom chatRoom, LocalDateTime lastReadTime);
}
