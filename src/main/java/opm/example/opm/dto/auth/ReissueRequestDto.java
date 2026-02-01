package opm.example.opm.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReissueRequestDto {
    private String refreshToken;
}