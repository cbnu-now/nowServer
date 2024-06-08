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
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            GroupBuyDto.GroupBuyInfo groupBuyInfo
    ) throws IOException {
        String url = null;

        // 만약 groupBuyInfo Dto에 null인 값이 있다면 에러 메시지를 반환한다.
        if (groupBuyInfo == null ||
                groupBuyInfo.getTitle() == null ||
                groupBuyInfo.getContent() == null ||
                groupBuyInfo.getCategory() == null ||
                groupBuyInfo.getLatitude() == null ||
                groupBuyInfo.getLongitude() == null) {
            return ResponseEntity.badRequest().body(UserDto.CheckResult.builder().result("모집글 정보가 없습니다.").build());
        }


        if (multipartFile != null)    {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }

        groupBuyService.createGroupBuy(groupBuyInfo, url);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());
    }


    // 모집글 조회 API 수정
    @GetMapping("/groupbuy/{groupBuyId}")
    @Operation(
            summary = "모집글 조회",
            description = "모집글의 id를 이용해 모집글 정보를 조회합니다."
    )
    public ResponseEntity<GroupBuyDto.viewGroupBuyInfo> getGroupBuy(@PathVariable Long groupBuyId) {
        return ResponseEntity.ok(groupBuyService.getGroupBuy(groupBuyId));
    }

    // 조기 마감 API 추가
    @PutMapping("/groupbuy/{groupBuyId}/close")
    @Operation(
            summary = "모집 조기 마감",
            description = "모집글을 조기 마감합니다."
    )
    public ResponseEntity<UserDto.CheckResult> closeEarly(@PathVariable Long groupBuyId) {
        groupBuyService.closeEarly(groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("조기 마감 완료").build());
    }



    @GetMapping("/groupbuy/list")
    @Operation(
            summary = "현재 위치의 모집글 리스트 조회",
            description = "모집글의 id를 이용해 모집글 정보를 조회합니다."
    )
    public ResponseEntity<List<GroupBuyDto.viewGroupBuyListInfo>> getGroupBuyListByLocation(Double Latitude, Double Longitude, Long distance) {
        List<GroupBuyDto.viewGroupBuyListInfo> groupBuyList = groupBuyService.getGroupBuyListByLocation(Latitude, Longitude,distance);
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

        if (multipartFile != null)    {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        groupBuyService.updateGroupBuy(groupBuyInfo, url, groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("수정 완료").build());
    }


    @DeleteMapping("/groupbuy/{groupBuyId}")
    @Operation(
            summary = "모집글 삭제",
            description = "모집글을 삭제합니다. 본인이 작성자면 삭제합니다."
    )
    public ResponseEntity<UserDto.CheckResult> deleteGroupBuy(@PathVariable Long groupBuyId) {
        groupBuyService.deleteGroupBuy(groupBuyId);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("삭제 완료").build());
    }

}
