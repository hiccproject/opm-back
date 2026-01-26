package HICC_Project.OnePageMe.handler;

import HICC_Project.OnePageMe.domain.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 로그인한 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. 사용자의 권한(Role) 확인하기
        // 우리가 만든 Role.GUEST의 key는 "ROLE_GUEST"입니다.
        boolean isGuest = oAuth2User.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.GUEST.getKey()));

        // 3. 역할에 따른 리다이렉트 (페이지 이동)
        if (isGuest) {
            // 손님(GUEST)이면 회원가입 추가 정보 입력 페이지로 이동
            response.sendRedirect("/signup");
        } else {
            // 일반 사용자(USER)면 메인 페이지로 이동
            response.sendRedirect("/");
        }
    }
}
