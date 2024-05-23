package hello.community.domain.chat;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.List;
import jakarta.persistence.OneToMany;

@Getter
@Setter
@Entity
public class ChatRoom {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> messages;
}
