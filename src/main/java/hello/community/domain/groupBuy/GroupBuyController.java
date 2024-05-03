package hello.community.domain.groupBuy;

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

@Tag(name = "GroupBuy", description = "모집글 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class GroupBuyController {
    private final GroupBuyService groupBuyService;
    private final S3Upload s3Upload;

    @PostMapping("/groupbuy")
    @Operation(
            summary = "모집글 등록",
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
    public ResponseEntity<UserDto.CheckResult> createGroupBuy(
            @RequestParam(value = "img") MultipartFile multipartFile,
            GroupBuyDto.GroupBuyInfo groupBuyInfo
    ) throws IOException {
        String url = null;
        // 만약 사진 파일이 없다면 에러 메시지를 반환한다.
        if (multipartFile == null || multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("이미지 파일이 없습니다.").build());
        }

        // 만약 groupBuyInfo Dto에 null인 값이 있다면 에러 메시지를 반환한다.
        if (groupBuyInfo == null ||
                groupBuyInfo.getTitle() == null ||
                groupBuyInfo.getContent() == null ||
                groupBuyInfo.getCategory() == null ||
                groupBuyInfo.getLatitude() == null ||
                groupBuyInfo.getLongitude() == null) {
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("모집글 정보가 없습니다.").build());
        }

        if (!multipartFile.isEmpty()) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        groupBuyService.createGroupBuy(groupBuyInfo, url);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());
    }


    @GetMapping("/groupbuy/{groupBuyId}")
    @Operation(
            summary = "모집글 조회",
            description = "모집글의 id를 이용해 모집글 정보를 조회합니다."
    )
    public ResponseEntity<GroupBuyDto.viewGroupBuyInfo> getGroupBuy(@PathVariable Long groupBuyId) {
        return ResponseEntity.ok(groupBuyService.getGroupBuy(groupBuyId));
    }



    @GetMapping("/groupbuy/list")
    @Operation(
            summary = "현재 위치의 모집글 리스트 조회",
            description = "모집글의 id를 이용해 모집글 정보를 조회합니다."
    )
    public ResponseEntity<List<GroupBuyDto.viewGroupBuyListInfo>> getGroupBuyListByLocation(Double Latitude, Double Longitude) {
        List<GroupBuyDto.viewGroupBuyListInfo> groupBuyList = groupBuyService.getGroupBuyListByLocation(Latitude, Longitude);
        return ResponseEntity.ok(groupBuyList);
    }

    @PostMapping("/groupbuy/like")
    @Operation(
            summary = "모집글 좋아요",
            description = "모집글의 좋아요 상태를 변경합니다. 좋아요를 누르면 좋아요가 증가하고, 다시 누르면 좋아요가 감소합니다."
    )
    public ResponseEntity<UserDto.CheckResult> likeGroupBuy(Long groupBuyId) {
        groupBuyService.likeGroupBuy(groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("좋아요 상태 변경 완료").build());
    }

    @PutMapping("/groupbuy/{groupBuyId}")
    @Operation(
            summary = "모집글 수정",
            description = "모집글을 수정합니다. 이때 사진은 멀티파트 폼데이터로 img라고 해서 보내야합니다.",
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
    public ResponseEntity<UserDto.CheckResult> updateGroupBuy(
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            GroupBuyDto.GroupBuyInfo groupBuyInfo,
            @PathVariable Long groupBuyId
    ) throws IOException {
        String url = null;

        if (!multipartFile.isEmpty()) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        groupBuyService.updateGroupBuy(groupBuyInfo, url, groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수정 완료").build());
    }


}
