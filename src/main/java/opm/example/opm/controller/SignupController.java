package opm.example.opm.controller;

import opm.example.opm.domain.Member;
import opm.example.opm.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class SignupController {

    private final MemberRepository memberRepository;

    // 1. 회원가입 페이지 보여주기 (GET)
    @GetMapping("/signup")
    public String signupPage(@AuthenticationPrincipal OAuth2User oauthUser, Model model) {
        // @AuthenticationPrincipal: 현재 로그인한 사용자의 정보를 가져옵니다.

        if (oauthUser != null) {
            // 구글에서 가져온 속성들
            String name = oauthUser.getAttribute("name");
            String email = oauthUser.getAttribute("email");

            // 모델에 담아서 HTML로 보냄 (자동 완성을 위해)
            model.addAttribute("name", name);
            model.addAttribute("email", email);
        }

        return "signup-form"; // signup-form.html을 보여줘라
    }

    // 2. 회원가입 정보 받아서 처리하기 (POST)
    @PostMapping("/signup")
    public String signupProcess(@AuthenticationPrincipal OAuth2User oauthUser,
                                @RequestParam("name") String name,
                                @RequestParam("password") String password) {

        String email = oauthUser.getAttribute("email"); // 이메일은 변경 불가 (식별자)

        // DB에서 회원 찾아서 정보 업데이트 (GUEST -> USER)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.completeSignup(name, password); // 엔티티에 만든 편의 메서드 사용
        memberRepository.save(member); // 변경사항 저장

        return "redirect:/"; // 메인 페이지로 이동
    }
}
