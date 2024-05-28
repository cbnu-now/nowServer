package hello.community.domain.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomDto {
    private Long id;
    private String chatRoomName;
}
