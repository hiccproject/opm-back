package opm.example.opm.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.MemberDetails;
import opm.example.opm.common.response.ApiResponse;
import opm.example.opm.common.response.PagedResponse;

import opm.example.opm.dto.portfolio.MyPortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioDetailResponseDto;
import opm.example.opm.dto.portfolio.PortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioSaveRequestDto;
import opm.example.opm.dto.portfolio.PortfolioReactionResponseDto;
import opm.example.opm.service.PortfolioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // 단계별 포트폴리오 저장
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Long>> savePortfolioStep(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(required = false) Long portfolioId,
            @RequestParam int step,
            @Valid @RequestBody PortfolioSaveRequestDto requestDto) {

        Long savedId = portfolioService.saveStep(memberDetails.getMember().getId(), portfolioId, requestDto, step);

        return ResponseEntity.ok(ApiResponse.success(savedId));
    }

    // 나의 포트폴리오 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<MyPortfolioListResponseDto>> getMyPortfolios(
            @AuthenticationPrincipal MemberDetails memberDetails) { // 현재 로그인 유저 정보

        List<MyPortfolioListResponseDto> responses = portfolioService
                .getMyPortfolios(memberDetails.getMember().getId());

        return ResponseEntity.ok(responses);
    }

    // 포트폴리오 상세 조회
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PortfolioDetailResponseDto>> getPortfolioDetail(
            @PathVariable String slug,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        PortfolioDetailResponseDto response = portfolioService.getPortfolioDetail(slug, memberDetails);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 포트폴리오 공유 링크 생성
    @GetMapping("/{portfolioId}/share-link")
    public ResponseEntity<ApiResponse<String>> getShareLink(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long portfolioId) {
        String shareLink = portfolioService.generateShareLink(memberDetails.getMember().getId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success(shareLink));
    }

    // 포트폴리오 목록 조회 (카테고리 및 태그별 필터링 가능)
    @GetMapping("/list")
    public ResponseEntity<PagedResponse<PortfolioListResponseDto>> getListPortfolios(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Parameter(description = "직업 대분류 (예: DEVELOPMENT, DESIGN) - 다중 선택 가능", example = "[\"DEVELOPMENT\", \"DESIGN\"]") @RequestParam(value = "category", required = false) List<String> categories,

            @Parameter(description = "해시태그 검색 - 다중 선택 가능", example = "[\"java\", \"spring\"]") @RequestParam(value = "tag", required = false) List<String> tags,
            @Parameter(description = "정렬 기준 (LATEST: 최신순, OLDEST: 등록순, POPULAR: 전체 인기순, REALTIME: 일일 인기순)", example = "LATEST") @RequestParam(value = "sort", required = false, defaultValue = "LATEST") String sort,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        Long memberId = (memberDetails != null) ? memberDetails.getMember().getId() : null;

        // 1. 서비스에서 Page<PortfolioListResponseDto>를 받아옵니다.
        org.springframework.data.domain.Page<PortfolioListResponseDto> portfolioPage = portfolioService
                .getPortfolioList(memberId, categories, tags, sort,
                        pageable);

        // 2. PagedResponse.from 메서드를 사용하여 커스텀 응답 객체로 변환합니다.
        PagedResponse<PortfolioListResponseDto> response = PagedResponse.from(portfolioPage);

        return ResponseEntity.ok(response);
    }

    // 포트폴리오 삭제 요청
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<String>> deletePortfolio(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long portfolioId) {
        portfolioService.deletePortfolio(memberDetails.getMember().getId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오가 성공적으로 삭제되었습니다."));
    }

    // 포트폴리오 상태 변경 (공개/비공개)
    @PatchMapping("/{portfolioId}/status")
    public ResponseEntity<ApiResponse<String>> updatePortfolioStatus(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long portfolioId,
            @RequestBody java.util.Map<String, String> request) {

        String statusStr = request.get("status");
        opm.example.opm.domain.portfolio.PortfolioStatus status = opm.example.opm.domain.portfolio.PortfolioStatus
                .valueOf(statusStr.toUpperCase());

        portfolioService.updateStatus(memberDetails.getMember().getId(), portfolioId, status);

        return ResponseEntity.ok(ApiResponse.success("포트폴리오 상태가 변경되었습니다."));
    }

    // 좋아요 토글
    @PostMapping("/{portfolioId}/likes")
    public ResponseEntity<ApiResponse<PortfolioReactionResponseDto>> toggleLike(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long portfolioId) {
        PortfolioReactionResponseDto response = portfolioService.toggleLike(memberDetails.getMember().getId(),
                portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 스크랩 토글
    @PostMapping("/{portfolioId}/scraps")
    public ResponseEntity<ApiResponse<PortfolioReactionResponseDto>> toggleScrap(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long portfolioId) {
        PortfolioReactionResponseDto response = portfolioService.toggleScrap(memberDetails.getMember().getId(),
                portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내가 좋아요한 포트폴리오 목록 조회
    @GetMapping("/likes")
    public ResponseEntity<PagedResponse<PortfolioListResponseDto>> getLikedPortfolios(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        org.springframework.data.domain.Page<PortfolioListResponseDto> portfolioPage = portfolioService
                .getLikedPortfolios(memberDetails.getMember().getId(), pageable);
        return ResponseEntity.ok(PagedResponse.from(portfolioPage));
    }

    // 내가 스크랩한 포트폴리오 목록 조회
    @GetMapping("/scraps")
    public ResponseEntity<PagedResponse<PortfolioListResponseDto>> getScrapedPortfolios(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        org.springframework.data.domain.Page<PortfolioListResponseDto> portfolioPage = portfolioService
                .getScrapedPortfolios(memberDetails.getMember().getId(), pageable);
        return ResponseEntity.ok(PagedResponse.from(portfolioPage));
    }
}
