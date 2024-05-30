package hello.community.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WaitingNotificationDto {
    private String userName;
    private String userProfileImgUrl;
    private String groupBuyTitle;
    private String groupBuyThumbnailUrl;
    private LocalDateTime notificationTime;
    private Long maxParticipants;
    private Long currentParticipants;
}
