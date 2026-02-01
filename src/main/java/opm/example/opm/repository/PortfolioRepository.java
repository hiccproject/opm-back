package opm.example.opm.repository;

import opm.example.opm.domain.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // memberId로 조회하며, 수정 시간(updatedAt) 기준 내림차순 정렬
    List<Portfolio> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId);

    // slug로 포트폴리오 조회
    Optional<Portfolio> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
