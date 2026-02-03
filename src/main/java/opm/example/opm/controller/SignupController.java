package opm.example.opm.controller;

import lombok.RequiredArgsConstructor;
import opm.example.opm.domain.member.Member;
import opm.example.opm.dto.memberResponse.MemberResponseDto;
import opm.example.opm.dto.signup.SignupRequestDto;
import opm.example.opm.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class SignupController {

    private final MemberRepository memberRepository;

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
}
