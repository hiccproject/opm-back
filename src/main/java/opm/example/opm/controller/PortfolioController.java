package opm.example.opm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.MemberDetails;
import opm.example.opm.common.response.ApiResponse;
import opm.example.opm.dto.portfolio.MyPortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioDetailResponseDto;
import opm.example.opm.dto.portfolio.PortfolioSaveRequestDto;
import opm.example.opm.service.PortfolioService;
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

        List<MyPortfolioListResponseDto> responses = portfolioService.getMyPortfolios(memberDetails.getMember().getId());

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
    public ResponseEntity<ApiResponse<String>> getShareLink(@PathVariable Long portfolioId) {
        String shareLink = portfolioService.generateShareLink(portfolioId);
        return ResponseEntity.ok(ApiResponse.success(shareLink));
    }
}
