package opm.example.opm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opm.example.opm.common.response.ApiResponse;
import opm.example.opm.dto.auth.LoginRequestDto;
import opm.example.opm.dto.auth.LoginResponseDto;
import opm.example.opm.dto.auth.PasswordResetRequestDto;
import opm.example.opm.dto.auth.SignupRequestDto;
import opm.example.opm.service.MailService;
import opm.example.opm.service.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final MailService mailService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = memberService.login(requestDto);
        return ApiResponse.success(response);
    }

    @PostMapping("/password-reset")
    public String resetPassword(@RequestBody PasswordResetRequestDto request) {
        // 1. Redis에서 인증 여부 최종 확인 (메일 인증 성공 시 저장해둔 결과값 체크)
        boolean isVerified = mailService.verifyCode(request.getEmail(), request.getCode());

        if (!isVerified) {
            return "인증번호가 유효하지 않거나 만료되었습니다.";
        }

        // 2. 서비스 단에서 비밀번호 업데이트 로직 호출
        memberService.updatePassword(request.getEmail(), request.getNewPassword());

        return "비밀번호가 성공적으로 변경되었습니다.";
    }
}
