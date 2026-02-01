package opm.example.opm.controller;


import lombok.RequiredArgsConstructor;
import opm.example.opm.dto.auth.ReissueRequestDto;
import opm.example.opm.dto.auth.TokenResponseDto;
import opm.example.opm.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequestDto request) {
        return ResponseEntity.ok(authService.reissue(request.getRefreshToken()));
    }
}