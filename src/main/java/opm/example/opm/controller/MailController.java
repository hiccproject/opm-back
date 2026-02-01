package opm.example.opm.controller;

import lombok.RequiredArgsConstructor;
import opm.example.opm.service.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @GetMapping("/send") // 경로를 좀 더 직관적으로 변경
    public String sendMimeMessage(@RequestParam("email") String email) {
        // 1. 6자리 랜덤 인증번호 생성
        String authCode = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. 사용자가 입력한 email 주소와 생성된 authCode를 서비스로 전달
        mailService.sendMimeMessage(email, authCode);

        return email + " 주소로 인증번호를 보냈습니다: " + authCode;
    }

    // 인증번호 검증 API
    @GetMapping("/verify")
    public String verifyMail(@RequestParam("email") String email, @RequestParam("code") String code) {
        boolean isVerified = mailService.verifyCode(email, code);

        if (isVerified) {
            return "인증에 성공하였습니다!";
        } else {
            return "인증번호가 일치하지 않거나 만료되었습니다.";
        }
    }
}