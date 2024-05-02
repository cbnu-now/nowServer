package hello.community.domain.user;

import hello.community.domain.community.CommunityDto;
import hello.community.domain.groupBuy.GroupBuyDto;
import hello.community.global.s3.S3Upload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "User", description = "User 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final S3Upload s3Upload;

    @Operation(
            summary = "회원 가입",
            description = "회원가입을 합니다. 바로 회원가입이 되는 api 이니 프론트에서 전화번호와 닉네임 중복확인을 하고 넘겨야합니다."
    )
    @PostMapping("/user")
    public ResponseEntity<UserDto.CheckResult> join(UserDto.JoinInfo joinDto) {
        Long userId = userService.join(joinDto);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("회원가입 성공 " + "회원아이디: " + userId).build());
    }


    @Operation(
            summary = "전화번호 중복확인",
            description = "전화번호 중복확인입니다. 전화번호를 넘겨서 유저가 있으면 아이디를 넘겨주고 없으면 null을 넘겨줍니다."
    )
    @GetMapping("/user/phone")
    public ResponseEntity<UserDto.CheckResult> isExistPhone(UserDto.PhoneNumCheck phoneNumCheck) {
        Long userId = userService.isExistPhone(phoneNumCheck);
        if (userId == null) {
            return ResponseEntity.ok(UserDto.CheckResult.builder().result("null").build());
        }
        return ResponseEntity.ok(UserDto.CheckResult.builder().result(userId.toString()).build());
    }


    @DeleteMapping("/user")
    @Operation(
            summary = "유저 탈퇴하기",
            description = "토큰을 기반으로 유저를 삭제합니다. 삭제하면 가계부도 함께 삭제됩니다."
    )
    public ResponseEntity<UserDto.CheckResult> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("유저 탈퇴 완료").build());
    }

    @Operation(
            summary = "로그인",
            description = "로그인을 합니다. 이메일과 비밀번호를 json으로 넘기면, json으로 토큰을 제공합니다."
    )
    @PostMapping("/login")
    public ResponseEntity<UserDto.Token> login(String phone) {
        String jwt = userService.signIn(phone);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", jwt);
        return ResponseEntity.ok(UserDto.Token.builder().token(jwt).build());
    }


    // 토큰으로 본인 정보 가져오기
    @Operation(
            summary = "유저 정보 조회",
            description = "토큰을 기반으로 유저 정보를 조회합니다."
    )
    @GetMapping("/user")
    public ResponseEntity<Users> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }

    @Operation(
            summary = "닉네임 중복조회",
            description = "닉네임을 string으로 넘기면 중복확인을 합니다. 중복이면 true, 중복이 아니면 false를 반환합니다."
    )
    @GetMapping("/user/doublecheck")
    public ResponseEntity<Boolean> nameDoubleCheck(String name) {
        boolean isExistUser = userService.nameDobuleCheck(name);
        return ResponseEntity.ok(isExistUser);
    }


    @PostMapping("/user/update")
    @Operation(
            summary = "유저 정보 수정",
            description = "유저 정보를 수정합니다. 여기서 프로필 사진도 바꿀 수 있으며 원하는 정보만 보내면 됩니다. \n" +
                    "프로필 사진을 등록시킬때는 헤더에 토큰과 함께, 멀티파트 폼데이터로 img 라는 이름으로 보내야합니다.\n" +
                    "이름은 name이라는 이름으로 보내면 됩니다.",
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
            @RequestParam(value = "img", required = false) MultipartFile multipartFile,
            @RequestParam(value = "name", required = false) String name
    ) throws IOException {
        String url = null;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            long fileSize = multipartFile.getSize();
            url = s3Upload.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), fileSize);
        }
        userService.updateUserInfo(url, name);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("저장 완료").build());
    }

    @Operation(
            summary = "내가 쓴 모집글 조회",
            description = "내가 쓴 모집글을 조회합니다. 토큰만 있다면 이를 조회할 수 있습니다."
    )
    @GetMapping("/user/groupbuy")
    public ResponseEntity<List<GroupBuyDto.viewGroupBuyListInfo>> GetMyGroupBuyList() {
        List<GroupBuyDto.viewGroupBuyListInfo> groupBuyList = userService.getMyGroupBuyList();
        return ResponseEntity.ok(groupBuyList);
    }


    @Operation(
            summary = "내가 쓴 커뮤니티 글 조회",
            description = "내가 쓴 커뮤니티 글을 조회합니다. 토큰만 있다면 이를 조회할 수 있습니다."
    )
    @GetMapping("/user/community")
    public ResponseEntity<List<CommunityDto.viewCommunityListInfo>> GetMyCommunityLike() {
        List<CommunityDto.viewCommunityListInfo> commmunityList = userService.getMyCommunity();
        return ResponseEntity.ok(commmunityList);
    }


    @Operation(
            summary = "관심을 한 모집글 조회",
            description = "내가 쓴 모집글을 조회합니다. 토큰만 있다면 이를 조회할 수 있습니다."
    )
    @GetMapping("/user/groupbuy/like")
    public ResponseEntity<List<GroupBuyDto.viewGroupBuyListInfo>> GetMyGroupBuyLike() {
        List<GroupBuyDto.viewGroupBuyListInfo> groupBuyList = userService.getMyGroupBuyLikeList();
        return ResponseEntity.ok(groupBuyList);
    }


    @Operation(
            summary = "관심을 한 커뮤니티 글 조회",
            description = "내가 관심을  커뮤니티 글을 조회합니다. 토큰만 있다면 이를 조회할 수 있습니다."
    )
    @GetMapping("/user/community/like")
    public ResponseEntity<List<CommunityDto.viewCommunityListInfo>> GetMyCommunityList() {
        List<CommunityDto.viewCommunityListInfo> commmunityList = userService.getMyCommunityLike();
        return ResponseEntity.ok(commmunityList);
    }




}
