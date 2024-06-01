package hello.community.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatRecordDto {
    private String userProfileImgUrl;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
    private boolean isContinuousMessage;
    private boolean isMyMessage;
    private String chatRoomTitle;
}
