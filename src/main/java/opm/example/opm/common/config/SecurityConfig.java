package opm.example.opm.common.config;

import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.*;
import opm.example.opm.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDetailsService memberDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용으로 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 (필요시 별도 설정)
                .cors(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션 관리: STATELESS (JWT 사용)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, memberDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                // 요청별 인가 설정
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        // 공개 엔드포인트
                                        .requestMatchers(
                                                "/",
                                                "/v3/api-docs/**",
                                                "/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/favicon.ico",
                                                "/error",
                                                "/login/**",
                                                "/oauth2/**",
                                                "/actuator/**",
                                                "/api/members/**",
                                                "/api/mail/**",
                                                "/api/auth/reissue",
                                                "/css/**", "/images/**", "/js/**", "/h2-console/**")
                                        .permitAll()
                                        .requestMatchers("/signup").hasRole("GUEST") // GUEST만 /signup 접근 가능
                                        .requestMatchers("/s3/**").permitAll() //S3 이미지 업로드 접근 허용
                                        .requestMatchers("/api/portfolios/my").authenticated() // 내 목록은 인증 필수
                                        .requestMatchers(HttpMethod.GET, "/api/portfolios/{id}").permitAll() // 상세 조회는 비회원 허용 예정
                                        // 그 외 모든 요청은 인증 필요
                                        .anyRequest()
                                        .authenticated()
                )
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfo) -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // ★ 핵심: 로그인 성공 시 이 핸들러를 써라!
                )
                .exceptionHandling(handler -> handler
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패 시 처리
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
        // 프론트엔드 도메인과 로컬 테스트 주소를 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList(
                "https://www.onepageme.kr",
                "https://onepageme.kr",
                "https://api.onepageme.kr",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}