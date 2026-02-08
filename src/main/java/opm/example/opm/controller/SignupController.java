package opm.example.opm.controller;

import lombok.RequiredArgsConstructor;
import opm.example.opm.dto.signup.ConsentRequestDto;
import opm.example.opm.service.MemberService;
import opm.example.opm.domain.member.Member;
import opm.example.opm.dto.memberResponse.MemberResponseDto;
import opm.example.opm.dto.signup.SignupRequestDto;
import opm.example.opm.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class SignupController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // 회원가입 페이지 진입 시: 현재 구글 정보(이름, 이메일)를 JSON으로 줌
    @GetMapping("/api/signup/info")
    public ResponseEntity<?> getSignupInfo(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) return ResponseEntity.status(401).body("Unauthorized");

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        // JSON 반환: {"email": "...", "name": "..."}
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("name", name);

        return ResponseEntity.ok(response);
    }

    // 회원가입 완료 요청
    @PostMapping("/api/signup")
    public ResponseEntity<?> signupProcess(@AuthenticationPrincipal OAuth2User oauthUser,
                                           @RequestBody SignupRequestDto requestDto) { // JSON으로 받음

        String email = oauthUser.getAttribute("email");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        member.completeSignup(requestDto.getName(), requestDto.getPassword());
        memberRepository.save(member);

        Map<String, String> response = new HashMap<>();
        response.put("message", "가입 완료");
        response.put("redirectUrl", "/"); // 프론트가 이동할 곳

        return ResponseEntity.ok(response);
    }


    // [API] 약관 동의 처리 (회원가입 완료)
    @PostMapping("/api/signup/consent")
    public ResponseEntity<?> agreeTerms(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody ConsentRequestDto requestDto) {

        // 1. 토큰에서 이메일 추출
        String email = userDetails.getUsername();

        // 2. 서비스 호출 (동의 처리 및 등급 승격)
        memberService.agreeToTerms(
                email,
                requestDto.isPersonalInfoAgreement(),
                requestDto.isServiceTermsAgreement()
        );

        // 3. 성공 응답 (프론트엔드가 메인으로 이동하도록 유도)
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("redirectUrl", "/"); // 메인 페이지로 이동

        return ResponseEntity.ok(response);
    }

}
