package opm.example.opm.common.oauth;

import opm.example.opm.domain.Member;
import opm.example.opm.domain.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import opm.example.opm.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 로그인한 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email"); // 구글에서 제공하는 이메일 추출

        // 2. 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 3. DB에 RefreshToken 저장
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        member.updateRefreshToken(refreshToken);
        memberRepository.save(member); // DB 반영

        // 4. 사용자의 권한(Role) 확인하기
        // 우리가 만든 Role.GUEST의 key는 "ROLE_GUEST"입니다.
        boolean isGuest = oAuth2User.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.GUEST.getKey()));


        String targetUrl;

        // 5. 역할에 따른 리다이렉트 (페이지 이동)
        if (isGuest) {
            // 손님(GUEST)이면 회원가입 추가 정보 입력 페이지로 이동
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/signup")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();
        } else {
            // 일반 사용자(USER)면 메인 페이지로 이동
            targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/")
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();
        }
    }
}
