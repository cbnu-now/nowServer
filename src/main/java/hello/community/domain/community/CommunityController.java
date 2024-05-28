package hello.community.domain.community;

import hello.community.domain.groupBuy.GroupBuyDto;
import hello.community.domain.user.UserDto;
import hello.community.global.s3.S3Upload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Community", description = "커뮤니티 관련 API 입니다.")  //Swagger에서 API 문서화를 위한 어노테이션으로, 이 컨트롤러를 설명
@RestController //이 클래스가 RESTful 웹 서비스의 컨트롤러
@RequiredArgsConstructor    //Lombok 어노테이션으로, final이나 @NonNull 필드에 대해 생성자를 생성
public class CommunityController {
    private final S3Upload s3Upload;    //파일 업로드를 AWS S3에 처리하는 서비스
    private final CommunityService communityService;    //비즈니스 로직을 처리하는 서비스

    @PostMapping("/community")  // HTTP POST 요청을 처리하는 엔드포인트를 정의
    @Operation( // Swagger에서 이 메소드의 설명을 추가
            summary = "커뮤니티글 등록", // 메소드의 요약 설명
            description = "모집글을 등록합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.", // 메소드의 상세 설명
            requestBody = @RequestBody( // HTTP 요청의 바디를 받아옵니다. 여기서는 CommunityDto.CommunityInfo 객체로 매핑
                    content = @Content( // HTTP 요청의 콘텐츠 타입을 정의
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, // 멀티파트 폼데이터
                            schema = @Schema( // 스키마 정의
                                    type = "string", // 데이터 타입
                                    format = "binary" // 데이터 포맷
                            )
                    )
            )
    )
    public ResponseEntity<UserDto.CheckResult> createCommunity(
            @RequestParam(value = "img", required = false) MultipartFile multipartFile, // HTTP 요청의 파라미터를 받아옵니다. 여기서는 파일 업로드를 처리
            CommunityDto.CommunityInfo communityInfo // 커뮤니티 정보 DTO
    ) throws IOException { // 입출력 예외처리

        // 만약 communityInfo Dto에 null인 값이 있다면 에러 메시지를 반환합니다.
        if (communityInfo == null ||
                communityInfo.getTitle() == null ||
                communityInfo.getContent() == null ||
                communityInfo.getCategory() == null ||
                communityInfo.getLatitude() == null ||
                communityInfo.getLongitude() == null ||
                communityInfo.getAddress() == null) {
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("커뮤니티글 정보가 없습니다.").build()); // 잘못된 요청 응답
        }

        String url = null; // 파일 URL 초기화

        if (multipartFile != null) { // 파일이 존재하는 경우
            long fileSize = multipartFile.getSize(); // 파일 크기 가져오기
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize); // 파일을 S3에 업로드하고 URL 반환
        }

        communityService.createCommunity(communityInfo, url); // 커뮤니티 글 생성 서비스 호출
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build()); // 성공 응답
    }
}


    @PostMapping("/community/like")
    @Operation(
            summary = "커뮤니티 좋아요",
            description = "커뮤니티 글의 좋아요 상태를 변경합니다. 좋아요를 누르면 좋아요가 증가하고, 다시 누르면 좋아요가 감소합니다."
    )
    public ResponseEntity<UserDto.CheckResult> likeCommunity(Long communityId) {
        communityService.likeCommunity(communityId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("좋아요 상태 변경 완료").build());
    }


    @GetMapping("/community/{communityId}")
    @Operation(
            summary = "커뮤니티 글 조회",
            description = "커뮤니티의 id를 이용해 모집글 정보를 조회합니다."
    )
    public ResponseEntity<CommunityDto.viewGroupBuyInfo> getCommunity(@PathVariable Long communityId) { //URL 경로의 변수를 받아옵니다.
        CommunityDto.viewGroupBuyInfo communityInfo = communityService.getCommunity(communityId);
        return ResponseEntity.ok(communityInfo);
    }

    //특정 커뮤니티 글에 댓글을 작성합니다. 이미지가 포함된 경우 S3에 업로드합니다.
    @PostMapping("/comment/{communityId}")
    @Operation(
            summary = "댓글 작성",
            description = "댓글을 작성합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
    )
    public ResponseEntity<UserDto.CheckResult> createComment(
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            @PathVariable Long communityId, String content
    ) throws IOException {
        String url = null;

        if (multipartFile != null) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }

        communityService.createComment(communityId, content, url);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());

    }
    //특정 댓글을 수정합니다. 이미지가 포함된 경우 S3에 업로드합니다.
    @PutMapping("/comment/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "댓글을 수정합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
    )
    public ResponseEntity<UserDto.CheckResult> updateComment(
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            @PathVariable Long commentId, String content
    ) throws IOException {
        String url = null;

        if (multipartFile != null) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        communityService.updateComment(commentId, content, url);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수정 완료").build());
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "댓글의 id를 이용해 댓글을 삭제합니다."
    )
    public ResponseEntity<UserDto.CheckResult> deleteComment(@PathVariable Long commentId) {

        communityService.deleteComment(commentId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("삭제 완료").build());
    }

    @GetMapping("/community/list")
    @Operation(
            summary = "현재 위치의 커뮤니티 글 리스트 조회",
            description = "모집글의 id를 이용해 커뮤니티글 정보를 조회합니다."
    )
    public ResponseEntity<List<CommunityDto.viewCommunityListInfo>> getCommunityListByLocation(Double Latitude, Double Longitude) {
        List<CommunityDto.viewCommunityListInfo> commmunityList = communityService.getCommunityByLocation(Latitude, Longitude);
        return ResponseEntity.ok(commmunityList);
    }

    @PutMapping("/community/{communityId}")
    @Operation(
            summary = "커뮤니티 글 수정",
            description = "커뮤니티 글을 수정합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
    )
    public ResponseEntity<UserDto.CheckResult> updateCommunity(
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            CommunityDto.CommunityInfo communityInfo,
            @PathVariable Long communityId
    ) throws IOException {
        String url = null;

        if (multipartFile != null) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        communityService.updateCommunity(communityInfo, url, communityId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수정 완료").build());
    }


    @DeleteMapping("/community/{communityId}")
    @Operation(
            summary = "커뮤니티 글 삭제",
            description = "커뮤니티 글을 삭제합니다. 본인이 작성자면 삭제합니다."
    )
    public ResponseEntity<UserDto.CheckResult> deleteGroupBuy(@PathVariable Long communityId) {
        communityService.deleteCommunity(communityId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("삭제 완료").build());
    }

}
