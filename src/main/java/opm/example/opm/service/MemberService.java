package opm.example.opm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opm.example.opm.common.oauth.JwtTokenProvider;
import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.member.Role;
import opm.example.opm.dto.auth.LoginRequestDto;
import opm.example.opm.dto.auth.LoginResponseDto;
import opm.example.opm.dto.auth.SignupRequestDto;
import opm.example.opm.repository.MemberRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Long signup(SignupRequestDto requestDto) {
        // 1. 이메일 중복 검사
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호와 비밀번호 확인 일치 검사
        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 약관 동의 검사
        if (!requestDto.isTermsAgreed()) {
            throw new IllegalArgumentException("약관에 동의해야 합니다.");
        }

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 5. 유저 저장
        Member member = Member.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(encodedPassword) // 일반 가입은 암호화된 비번 저장
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        return member.getId();
    }


    // MemberService.java
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        // 1. 이메일로 유저 조회
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 일치 여부 검사
        boolean isMatch = passwordEncoder.matches(requestDto.getPassword(), member.getPassword());


        if (!isMatch) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 토큰 생성
        String AccessToken = jwtTokenProvider.createAccessToken(member.getEmail());
        String RefreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // DB에 RefreshToken 저장
        member.updateRefreshToken(RefreshToken);
        memberRepository.save(member);

        // DTO에 토큰을 실어서 반환
        return new LoginResponseDto(member.getId(), member.getName(), member.getEmail(), AccessToken, RefreshToken);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 새로운 비밀번호 암호화 후 저장
        member.updatePassword(passwordEncoder.encode(newPassword));

        // 인증 성공 후에는 Redis에서 인증번호를 삭제해 주는 것이 깔끔합니다.
        redisTemplate.delete(email);
    }

}
