package hello.community.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "User 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "회원 가입",
            description = "회원가입을 합니다. 바로 회원가입이 되는 api 이니 프론트에서 전화번호와 닉네임 중복확인을 하고 넘겨야합니다."
    )
    @PostMapping("/user")
    public ResponseEntity<UserDto.CheckResult> join(UserDto.JoinInfo joinDto) {
        Long userId = userService.join(joinDto);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("회원가입 성공 "+ "회원아이디: " + userId).build());
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

    // 위도 경도 등록
    @Operation(
            summary = "위도 경도 등록",
            description = "우리집의 위도 경도를 등록합니다."
    )
    @PostMapping("/user/location")
    public ResponseEntity<UserDto.CheckResult> setLocation(UserDto.Location location) {
        userService.setLocation(location);
        return ResponseEntity.ok(UserDto.CheckResult.builder().result("위도 경도 등록 완료").build());
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

}
