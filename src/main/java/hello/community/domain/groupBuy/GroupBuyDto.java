package hello.community.domain.groupBuy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
        private String address;
    }


    @Builder
    @Data
    @Schema(description = "모집글에 등록되어야할 정보")
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
    }

    @Builder
    @Data
    @Schema(description = "리스트로 모집글 보여줄때 필요한 정보")
    public static class viewGroupBuyListInfo {
        private String title;
        private String img;
        private String Category;
        private LocalDateTime createdAt;
        private Long headCount;
        private Long currentCount;
        private Long likes;
        private boolean isLiked;
    }
}
