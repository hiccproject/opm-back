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
import org.springframework.web.multipart.MultipartFile;
import opm.example.opm.dto.passwordChange.PasswordChangeRequestDto;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final S3Service s3Service; // S3 서비스 주입

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

    // ===============================================================

    // 회원 탈퇴 (이메일로 삭제)
    @Transactional
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        memberRepository.delete(member); // DB에서 삭제
    }

    // 계정 정보 수정 (이름 변경)
    @Transactional
    public void updateMember(String email, String newName) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 엔티티의 데이터를 변경
        member.updateInfo(newName);

        // ★ 중요: @Transactional이 붙어있다면,
        // memberRepository.save(member)를 호출하지 않아도
        // 메서드가 끝날 때 변경된 내용을 감지해서 DB에 자동으로 반영해줍니다. (Dirty Checking)
    }

    @Transactional
    public void updateProfilePicture(String email, MultipartFile file) throws IOException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 1. S3에 파일 업로드하고 URL 받아오기
        String pictureUrl = s3Service.uploadImage(file);

        // 2. DB에 이미지 URL 저장 (Dirty Checking)
        member.updateProfile(pictureUrl);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, PasswordChangeRequestDto requestDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호 암호화 및 변경
        member.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }

    // [회원가입 마무리] 약관 동의 및 정회원 승격
    public void agreeToTerms(String email, boolean personalInfo, boolean serviceTerms, String name) {
        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 2. 필수 약관 동의 확인 (백엔드에서도 한 번 더 검증)
        if (!personalInfo || !serviceTerms) {
            throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다.");
        }

        // 3. 이름 저장 (업데이트)
        if (name != null && !name.trim().isEmpty()) {
            member.updateInfo(name);
        } else {
            throw new IllegalArgumentException("이름은 필수 입력값입니다.");
        }

        // 3. 역할 변경 (GUEST -> USER)
        // Member 엔티티에 updateRole 메서드가 없다면 추가해주세요!
        member.authorizeUser();
    }

}
