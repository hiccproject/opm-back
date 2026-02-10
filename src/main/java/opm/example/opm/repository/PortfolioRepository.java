package opm.example.opm.repository;

import opm.example.opm.domain.portfolio.OccupationCategory;
import opm.example.opm.domain.portfolio.Portfolio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
        // memberId로 조회하며, 수정 시간(updatedAt) 기준 내림차순 정렬
        List<Portfolio> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId);

        // slug로 포트폴리오 조회
        Optional<Portfolio> findBySlug(String slug);

        boolean existsBySlug(String slug);

        // 카테고리 및 태그 필터링 조회 (무한 스크롤 & 다중 선택) -> 페이지네이션으로 변경
        @Query("SELECT p FROM Portfolio p " +
                        "WHERE p.status = 'PUBLISHED' " +
                        "AND (:categories IS NULL OR p.category IN :categories) " +
                        "AND (:tags IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t IN :tags))")
        org.springframework.data.domain.Page<Portfolio> findPublishedPortfolios(
                        @Param("categories") List<OccupationCategory> categories,
                        @Param("tags") List<String> tags,
                        Pageable pageable);
}
