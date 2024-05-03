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

@Tag(name = "Community", description = "커뮤니티 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final S3Upload s3Upload;
    private final CommunityService communityService;

    @PostMapping("/community")
    @Operation(
            summary = "커뮤니티글 등록",
            description = "모집글을 등록합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.",
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
    public ResponseEntity<UserDto.CheckResult> createCommunity(
            @RequestParam(value = "img") MultipartFile multipartFile,
            CommunityDto.CommunityInfo communityInfo
    ) throws IOException {
        // 만약 사진 파일이 없다면 에러 메시지를 반환한다.
        if (multipartFile == null || multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("이미지 파일이 없습니다.").build());
        }

        // 만약 groupBuyInfo Dto에 null인 값이 있다면 에러 메시지를 반환한다.
        if (communityInfo == null ||
                communityInfo.getTitle() == null ||
                communityInfo.getContent() == null ||
                communityInfo.getCategory() == null ||
                communityInfo.getLatitude() == null ||
                communityInfo.getLongitude() == null ||
                communityInfo.getAddress() == null){
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("커뮤니티글 정보가 없습니다.").build());
        }

        String url = null;
        if (!multipartFile.isEmpty()) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }

        communityService.createCommunity(communityInfo, url);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());
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
    public ResponseEntity<CommunityDto.viewGroupBuyInfo> getCommunity(@PathVariable Long communityId) {
        CommunityDto.viewGroupBuyInfo communityInfo = communityService.getCommunity(communityId);
        return ResponseEntity.ok(communityInfo);
    }

    @PostMapping("/comment/{communityId}")
    @Operation(
            summary = "댓글 작성",
            description = "커뮤니티의 id를 이용해 댓글을 작성합니다."
    )
    public ResponseEntity<UserDto.CheckResult> createComment(@PathVariable Long communityId, String content) {
        communityService.createComment(communityId, content);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());

    }

    @PutMapping("/comment/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "댓글의 id를 이용해 댓글을 수정합니다."
    )
    public ResponseEntity<UserDto.CheckResult> updateComment(@PathVariable Long commentId, String content) {
        communityService.updateComment(commentId, content);
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

        if (multipartFile != null)    {
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
        communityService.deleteGroupBuy(communityId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("삭제 완료").build());
    }

}
