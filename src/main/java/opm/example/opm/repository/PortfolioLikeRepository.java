package opm.example.opm.repository;

import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.portfolio.Portfolio;
import opm.example.opm.domain.portfolio.PortfolioLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioLikeRepository extends JpaRepository<PortfolioLike, Long> {
    Optional<PortfolioLike> findByMemberAndPortfolio(Member member, Portfolio portfolio);

    boolean existsByMemberAndPortfolio(Member member, Portfolio portfolio);
}
