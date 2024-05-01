package hello.community.domain.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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


    @Builder
    @Data
    @Schema(description = "커뮤니티에서 조회할때 줄 정보")
    public static class viewGroupBuyInfo {
        private String writerName;
        private String writerImg;
        private String address;

        private String title;
        private String img;
        private String content;
        private String Category;
        private LocalDateTime createdAt;
        private Long headCount;
        private Long currentCount;

        private Long view;
        private Long likes;
        private List<Comment> comments;
    }
}
