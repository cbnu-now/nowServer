package hello.community.domain.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
public class CommunityDto {

    @Builder
    @Data
    @Schema(description = "커뮤니티글에 등록되어야할 정보")
    public static class CommunityInfo {
        private String title;
        private String content;
        private String Category;
        private Double latitude;
        private Double longitude;
        private String address;
    }
}
