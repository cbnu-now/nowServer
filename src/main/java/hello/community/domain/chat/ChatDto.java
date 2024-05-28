package hello.community.domain.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String username;
    private String userProfileImageUrl;
    private boolean isMyMessage;
}
