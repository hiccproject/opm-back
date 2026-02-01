package opm.example.opm.domain.portfolio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioViewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    private LocalDate viewDate; // 조회 날짜

    private Integer dailyCount; // 해당 일자의 조회수

    @Builder
    public PortfolioViewLog(Portfolio portfolio, LocalDate viewDate) {
        this.portfolio = portfolio;
        this.viewDate = viewDate;
        this.dailyCount = 1;
    }

    public void increment() {
        this.dailyCount++;
    }
}