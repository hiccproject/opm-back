package opm.example.opm.repository;

import opm.example.opm.domain.portfolio.Portfolio;
import opm.example.opm.domain.portfolio.PortfolioViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PortfolioViewLogRepository extends JpaRepository<PortfolioViewLog, Long> {
    Optional<PortfolioViewLog> findTopByPortfolioAndViewDate(Portfolio portfolio, LocalDate viewDate);
}