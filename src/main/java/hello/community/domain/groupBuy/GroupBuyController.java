package hello.community.domain.groupBuy;

import hello.community.domain.user.UserDto;
import hello.community.domain.user.UserService;
import hello.community.global.s3.S3Upload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "GroupBuy", description = "모집글 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class GroupBuyController {
    private final GroupBuyService groupBuyService;
    private final S3Upload s3Upload;

    @PostMapping("/groupbuy")
    @Operation(
            summary = "모집글 등록",
            description = "모집글을 등록합니다. 이때 사진은 멀티파트 폼데이터로 photo라고 해서 보내야합니다.",
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
    public ResponseEntity<UserDto.CheckResult> updateUserInfo(
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
}
