package opm.example.opm.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import opm.example.opm.domain.portfolio.Portfolio;
import opm.example.opm.domain.portfolio.PortfolioStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyPortfolioListResponseDto {
    private Long id;
    private String title;
    private String profileImg;
    private PortfolioStatus status;
    private Integer lastStep;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static MyPortfolioListResponseDto fromEntity(Portfolio portfolio) {
        // 제목이 따로 없다면 카테고리 조합으로 생성
        String displayTitle = (portfolio.getCategory() != null)
                ? portfolio.getCategory() + " " + portfolio.getSubCategory()
                : "새로운 포트폴리오";

        return new MyPortfolioListResponseDto(
                portfolio.getPortfolioId(),
                portfolio.getCategory() + " - " + portfolio.getSubCategory(),
                portfolio.getProfileImg(),
                portfolio.getStatus(),
                portfolio.getLastStep(),
                portfolio.getUpdatedAt() // 엔티티에 업데이트 시간 필드가 있다면 사용
        );
    }
}
