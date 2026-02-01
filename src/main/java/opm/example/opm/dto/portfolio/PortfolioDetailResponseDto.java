package opm.example.opm.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import opm.example.opm.domain.portfolio.LayoutType;
import opm.example.opm.domain.portfolio.Portfolio;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PortfolioDetailResponseDto {
    private Long id;
    private String category;
    private String subCategory;
    private String profileImg;
    private String email;
    private String phone;
    private String location;
    private List<PortfolioSaveRequestDto.ProjectDto> projects;
    private String summaryIntro;
    private LayoutType layoutType;

    // 추가 관리 데이터
    private Integer viewCount;
    private boolean isOwner;
    private LocalDateTime updatedAt;

    public static PortfolioDetailResponseDto fromEntity(Portfolio portfolio, boolean isOwner) {
        return PortfolioDetailResponseDto.builder()
                .id(portfolio.getPortfolioId())
                .category(portfolio.getCategory())
                .subCategory(portfolio.getSubCategory())
                .profileImg(portfolio.getProfileImg())
                .email(portfolio.getEmail())
                .phone(portfolio.getPhone())
                .location(portfolio.getLocation())
                .summaryIntro(portfolio.getSummaryIntro())
                .layoutType(portfolio.getLayoutType())
                .viewCount(isOwner ? portfolio.getViewCount() : null)
                .isOwner(isOwner)
                .updatedAt(portfolio.getUpdatedAt())
                .projects(portfolio.getProjects().stream()
                        .map(p -> new PortfolioSaveRequestDto.ProjectDto(p.getProjectName(), p.getProjectSummary(), p.getProjectLink()))
                        .toList())
                .build();
    }
}