package HICC_Project.OnePageMe.config;

import HICC_Project.OnePageMe.handler.OAuth2LoginSuccessHandler; // ★ 추가
import HICC_Project.OnePageMe.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // ★ 추가 (주입 받기)

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .headers((headers) -> headers.frameOptions((frame) -> frame.disable()))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**").permitAll()
                        .requestMatchers("/signup").hasRole("GUEST") // GUEST만 /signup 접근 가능
                        .anyRequest().authenticated()
                )
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfo) -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // ★ 핵심: 로그인 성공 시 이 핸들러를 써라!
                );

        return http.build();
    }
}
