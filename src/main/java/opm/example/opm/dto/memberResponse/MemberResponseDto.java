package opm.example.opm.dto.memberResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import opm.example.opm.domain.member.Member;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponseDto {
    private String name;
    private String email;
    private String picture;
    private String role;

    // Entity -> DTO 변환 메서드
    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .name(member.getName())
                .email(member.getEmail())
                .picture(member.getPicture())
                .role(member.getRole().getTitle())
                .build();
    }
}