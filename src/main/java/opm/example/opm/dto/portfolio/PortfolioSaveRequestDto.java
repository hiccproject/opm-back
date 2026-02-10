package opm.example.opm.dto.portfolio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import opm.example.opm.domain.portfolio.LayoutType;
import opm.example.opm.domain.portfolio.OccupationCategory;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSaveRequestDto {

    // Step 1: 직군 및 프로필
    private OccupationCategory category;
    private String subCategory;
    private String profileImg;

    // Step 2: 연락처 정보\
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phone;
    private String location;

    // Step 3: 프로젝트 리스트
    private List<@Valid ProjectDto> projects;

    // Step 4: 소개글
    @Size(max = 500, message = "소개글은 최대 500자까지 입력 가능합니다.")
    private String summaryIntro;

    // Step 5: 레이아웃 형태
    private LayoutType layoutType;

    private String slug; // 사용자가 정할 커스텀 URL 주소
    private List<String> tags; // 포트폴리오와 연관된 태그 리스트

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDto {
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        private String projectName;
        @NotBlank(message = "프로젝트 설명은 필수입니다.")
        @Size(max = 200, message = "프로젝트 설명은 최대 200자까지 입력 가능합니다.")
        private String projectSummary;
        private String projectImg;
        private String projectLink;
    }
}