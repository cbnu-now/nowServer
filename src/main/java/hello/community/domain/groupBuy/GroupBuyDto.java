package hello.community.domain.groupBuy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
public class GroupBuyDto {

    @Builder
    @Data
    @Schema(description = "모집글에 등록되어야할 정보")
    public static class GroupBuyInfo {
        private String title;
        private String content;
        private Double latitude;
        private Double longitude;
        private String Category;
        private Long headCount;
    }
}
