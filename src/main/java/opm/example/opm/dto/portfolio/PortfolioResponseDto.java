package opm.example.opm.dto.portfolio;

import lombok.Builder;
import lombok.Getter;
import opm.example.opm.domain.portfolio.LayoutType;
import opm.example.opm.domain.portfolio.Portfolio;

import java.util.List;

@Getter
@Builder
public class PortfolioResponseDto {
    private Long portfolioId;
    private String category;
    private String subCategory;
    private String profileImg;
    private String email;
    private String phone;
    private String location;
    private List<PortfolioSaveRequestDto.ProjectDto> projects;
    private String summaryIntro;
    private LayoutType layoutType;
    private Integer lastStep;

    public static PortfolioResponseDto fromEntity(Portfolio portfolio) {
        return PortfolioResponseDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .category(portfolio.getCategory())
                .subCategory(portfolio.getSubCategory())
                .profileImg(portfolio.getProfileImg())
                .email(portfolio.getEmail())
                .phone(portfolio.getPhone())
                .location(portfolio.getLocation())
                .summaryIntro(portfolio.getSummaryIntro())
                .layoutType(portfolio.getLayoutType())
                .lastStep(portfolio.getLastStep())
                .projects(portfolio.getProjects().stream()
                        .map(p -> new PortfolioSaveRequestDto.ProjectDto(p.getProjectName(), p.getProjectSummary(), p.getProjectLink()))
                        .toList())
                .build();
    }
}