package opm.example.opm.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey = "vmfhaltskswlqdptjvusvmfhmetmzpdlfhaltskswlqdptjvusvmfhmetmzpdlfha";
    private final long accessTokenValidity = 1000L * 60 * 60; // 1시간
    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 14; // 14일

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // AccessToken 생성 (빌더 패턴 통일)
    public String createAccessToken(String email) {
        return createToken(email, accessTokenValidity);
    }

    // RefreshToken 생성 (빌더 패턴 통일)
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenValidity);
    }

    // 공통 토큰 생성 로직 (중복 제거)
    private String createToken(String email, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email) // setClaims 대신 직접 subject 설정 권장
                .issuedAt(now)
                .expiration(validity)
                .signWith(key) // 알고리즘은 키 크기에 따라 자동 선택됨
                .compact();
    }

    // 토큰 유효성 검증 (재발급 API를 위해 필수 추가)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 이메일 추출 (재발급 API를 위해 필수 추가)
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
