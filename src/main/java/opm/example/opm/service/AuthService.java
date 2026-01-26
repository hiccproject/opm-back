package opm.example.opm.service;

import lombok.RequiredArgsConstructor;
import opm.example.opm.common.JwtTokenProvider;
import opm.example.opm.domain.Member;
import opm.example.opm.dto.TokenResponseDto;
import opm.example.opm.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponseDto reissue(String refreshToken) {
        // 1. RefreshToken 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3. DB에 저장된 토큰과 비교
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!member.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("토큰 정보가 일치하지 않습니다.");
        }

        // 4. 새로운 토큰들 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        // 5. DB의 RefreshToken 업데이트 (보안 강화: Rotation 방식)
        member.updateRefreshToken(newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}