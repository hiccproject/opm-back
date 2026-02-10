package opm.example.opm.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import opm.example.opm.domain.portfolio.LayoutType;
import opm.example.opm.domain.portfolio.OccupationCategory;
import opm.example.opm.domain.portfolio.Portfolio;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PortfolioDetailResponseDto {
        private Long id;
        private OccupationCategory category;
        private String subCategory;
        private String profileImg;
        private String email;
        private String phone;
        private String location;
        private List<PortfolioSaveRequestDto.ProjectDto> projects;
        private String summaryIntro;
        private LayoutType layoutType;
        private List<String> tags;

        // 추가 관리 데이터
        private boolean isOwner;
        private LocalDateTime updatedAt;

        // 조회수 관련 필드
        private Integer totalViewCount; // 누적 조회수
        private Integer todayViewCount; // 오늘 하루 조회수
        private String username;

        public static PortfolioDetailResponseDto fromEntity(Portfolio portfolio, boolean isOwner,
                        Integer todayViewCount) {
                return PortfolioDetailResponseDto.builder()
                                .id(portfolio.getPortfolioId())
                                .username(portfolio.getMember().getName())
                                .category(portfolio.getCategory())
                                .subCategory(portfolio.getSubCategory())
                                .profileImg(portfolio.getProfileImg())
                                .email(portfolio.getEmail())
                                .phone(portfolio.getPhone())
                                .location(portfolio.getLocation())
                                .summaryIntro(portfolio.getSummaryIntro())
                                .tags(portfolio.getTags())
                                .layoutType(portfolio.getLayoutType())
                                .isOwner(isOwner)
                                .updatedAt(portfolio.getUpdatedAt())
                                .projects(portfolio.getProjects().stream()
                                                .map(p -> PortfolioSaveRequestDto.ProjectDto.builder()
                                                                .projectName(p.getProjectName())
                                                                .projectSummary(p.getProjectSummary())
                                                                .projectImg(p.getProjectImg())
                                                                .projectLink(p.getProjectLink())
                                                                .build())
                                                .toList())
                                .totalViewCount(isOwner ? portfolio.getViewCount() : null)
                                .todayViewCount(isOwner ? todayViewCount : null)
                                .build();
        }
}