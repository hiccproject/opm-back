package opm.example.opm.dto.signup;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConsentRequestDto {

    // 개인정보 수집 이용 동의 (필수)
    private boolean personalInfoAgreement;

    // 이용약관 동의 (필수)
    private boolean serviceTermsAgreement;

    // 이름 (필수)
    private String name;

    // 마케팅 수신 동의 (선택)
    // private boolean marketingAgreement;
}
