package opm.example.opm.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioReactionResponseDto {
    private boolean isLiked;
    private int likeCount;
    private boolean isScraped;
    private int scrapCount;
}
