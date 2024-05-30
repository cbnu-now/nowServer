package hello.community.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE c.chatRoom.id = :chatRoomId ORDER BY c.createdAt DESC")
    List<Chat> findLatestChatByChatRoomId(Long chatRoomId);

    @Query("SELECT COUNT(c) FROM Chat c WHERE c.chatRoom.id = :chatRoomId AND c.user.id <> :userId AND c.createdAt > (SELECT ucr.lastReadTime FROM UserChatRoom ucr WHERE ucr.chatRoom.id = :chatRoomId AND ucr.user.id = :userId)")
    int countUnreadMessages(Long chatRoomId, Long userId);
}
