package opm.example.opm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opm.example.opm.common.response.ApiResponse;
import opm.example.opm.dto.auth.LoginRequestDto;
import opm.example.opm.dto.auth.LoginResponseDto;
import opm.example.opm.dto.auth.SignupRequestDto;
import opm.example.opm.service.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

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
}
