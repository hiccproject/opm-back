package opm.example.opm.repository;

import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.portfolio.Portfolio;
import opm.example.opm.domain.portfolio.PortfolioLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PortfolioLikeRepository extends JpaRepository<PortfolioLike, Long> {
    Optional<PortfolioLike> findByMemberAndPortfolio(Member member, Portfolio portfolio);

    boolean existsByMemberAndPortfolio(Member member, Portfolio portfolio);

    @Query("SELECT l.portfolio FROM PortfolioLike l WHERE l.member.id = :memberId")
    Page<Portfolio> findLikedPortfoliosByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
