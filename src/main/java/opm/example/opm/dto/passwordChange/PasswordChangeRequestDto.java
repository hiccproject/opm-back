package opm.example.opm.dto.passwordChange;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
}
