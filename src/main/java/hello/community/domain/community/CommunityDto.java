package hello.community.domain.community;

import io.swagger.v3.oas.annotations.media.Schema;  //Swagger/OpenAPI의 스키마 어노테이션을 사용하여 클래스와 필드에 대한 설명을 제공
import lombok.Builder;
import lombok.Data; //어노테이션을 사용하여 모든 필드에 대한 getter, setter, toString, equals, hashCode 메서드를 자동으로 생성

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityDto {

    @Builder    //빌더 패턴을 통해 객체를 유연하게 생성
    @Data   //Lombok을 사용하여 모든 필드에 대한 getter, setter, toString, equals, hashCode 메서드를 자동으로 생성
    @Schema(description = "커뮤니티글에 등록되어야할 정보")   //Swagger/OpenAPI 문서에 이 클래스가 커뮤니티 글 등록 시 사용되는 정보를 담고 있음을 설명
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
        private boolean isWriter;

        private Long view;
        private Long likes;
        private List<CommentComponent> comments;
    }

    @Builder
    @Data
    public static class CommentComponent{
        private Long id;
        private String content;
        private String writerName;
        private String writerImg;
        private LocalDateTime createdAt;
        private String who;
        private String img;
    }

    @Builder
    @Data
    @Schema(description = "리스트로 커뮤니티글 보여줄때 필요한 정보")
    public static class viewCommunityListInfo {
        private Long id;
        private String title;
        private String img;
        private String category;
        private LocalDateTime createdAt;
        private String address;
        private String content;
        private Long likes;
    }
}
