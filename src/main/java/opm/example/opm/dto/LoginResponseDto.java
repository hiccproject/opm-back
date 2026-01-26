package opm.example.opm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String name;
    private String email;
    private String accessToken;
    private String refreshToken;
}
