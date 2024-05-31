package hello.community.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {

    @Builder
    @Data
    @Schema(description = "회원 가입시 넘겨주는 정보")
    public static class JoinInfo {
        String phone;
        String name;
    }

    @Builder
    @Data
    @Schema(description = "일치여부")
    public static class CheckResult {
        String result;
    }

    @Builder
    @Data
    @Schema(description = "휴대폰 정보")
    public static class PhoneNumCheck {
        String phone;
    }

    @Builder
    @Data
    @Schema(description = "토큰 정보")
    public  static class Token {
        String token;
    }


    @Builder
    @Data
    @Schema(description = "위도 경도정보")
    public  static class Location {
        String latitude;
        String longitude;
    }

    @Builder
    @Data
    @Schema(description = "개인정보")
    public static class ProvideInfo {
        String name;
        String photo;
        String phone;
        Long id;

    }
}
