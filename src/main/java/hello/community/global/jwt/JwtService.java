package hello.community.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.spec.SecretKeySpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    final String INVALID_JWT = "유효하지 않은 토큰입니다.";
    final String EMPTY_JWT = "토큰이 비어있습니다.";


    private final String secretKey;
    private final long expirationHours;
    private final String issuer;

    public JwtService(
            @Value("${jwt.token.secret-key}") String secretKey,
            @Value("${jwt.token.expiration-hours}") long expirationHours,
            @Value("${jwt.token.issuer}") String issuer
    ) {
        this.secretKey = secretKey;
        this.expirationHours = expirationHours;
        this.issuer = issuer;
    }


    public String createToken(Long userId) {
        return Jwts.builder()
                .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()))   // HS512 알고리즘을 사용하여 secretKey를 이용해 서명
                .setSubject(userId.toString())  // JWT 토큰 제목
                .setIssuer(issuer)  // JWT 토큰 발급자
                .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))    // JWT 토큰 발급 시간
                .setExpiration(Date.from(Instant.now().plus(expirationHours, ChronoUnit.HOURS)))    // JWT 토큰 만료 시간
                .compact(); // JWT 토큰 생성
    }


    //헤더에서 토큰 추출
    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("X-ACCESS-TOKEN");
    }

    //토큰에서 id 추출
    public Long getUserId(String token) throws Exception {
        // 헤더에서 JWT 추출
        String accessToken = token;
        if(accessToken == null || accessToken.length() == 0){
            throw new Exception(EMPTY_JWT);
        }

        // JWT 파싱
        Jws<Claims> claims;
        try{
            claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(accessToken);
        } catch (Exception ignored) {
            throw new Exception(INVALID_JWT);
        }

        // id 추출
        return Long.parseLong(claims.getBody().getSubject());
    }


    public String validateTokenAndGetSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
