package opm.example.opm.dto.signup;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    private String name;
    private String password;
}
