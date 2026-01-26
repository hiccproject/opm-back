package opm.example.opm.common.config;

import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.OAuth2LoginSuccessHandler;
import opm.example.opm.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // ★ 추가 (주입 받기)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용으로 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 (필요시 별도 설정)
                .cors(AbstractHttpConfigurer::disable)
                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션 관리: STATELESS (JWT 사용)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 인가 설정
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        // 공개 엔드포인트
                                        .requestMatchers(
                                                "/",
                                                "/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/actuator/**",
                                                "/api/members/**",
                                                "/api/auth/reissue",
                                                "/css/**", "/images/**", "/js/**", "/h2-console/**")
                                        .permitAll()
                                        .requestMatchers("/signup").hasRole("GUEST") // GUEST만 /signup 접근 가능
                                        // 그 외 모든 요청은 인증 필요
                                        .anyRequest()
                                        .authenticated()
                )
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfo) -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // ★ 핵심: 로그인 성공 시 이 핸들러를 써라!
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 허용할 도메인 설정 (배포 주소와 로컬 주소 모두 포함)
        configuration.setAllowedOrigins(Arrays.asList("https://onepageme.kr", "http://localhost:8080"));

        // 2. 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 4. 자격 증명 허용 (쿠키, 인증 헤더 등)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}