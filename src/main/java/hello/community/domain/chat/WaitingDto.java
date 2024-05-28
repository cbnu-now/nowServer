package hello.community.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaitingDto {
    private Long id;
    private String userName;
    private String userPhoto;
    private Long groupBuyId;
    private String groupBuyTitle;
}
