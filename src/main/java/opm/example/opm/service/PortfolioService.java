package opm.example.opm.service;

import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.MemberDetails;
import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.portfolio.*;
import opm.example.opm.dto.portfolio.MyPortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioDetailResponseDto;
import opm.example.opm.dto.portfolio.PortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioSaveRequestDto;
import opm.example.opm.repository.MemberRepository;
import opm.example.opm.repository.PortfolioRepository;
import opm.example.opm.repository.PortfolioViewLogRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;
    private final PortfolioViewLogRepository viewLogRepository;

    @Value("${app.domain}")
    private String domain;

    // 단계별 포트폴리오 저장 메서드
    @Transactional
    public Long saveStep(Long memberId, Long portfolioId, PortfolioSaveRequestDto requestDto, int step) {
        Portfolio portfolio;

        if (portfolioId == null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            String randomSlug = java.util.UUID.randomUUID().toString().substring(0, 8);

            // 객체를 먼저 생성 (필수 값인 1단계 값들을 빌더에 포함하는 것이 안전합니다)
            portfolio = Portfolio.builder()
                    .member(member)
                    .slug(randomSlug)
                    .category(requestDto.getCategory())
                    .subCategory(requestDto.getSubCategory())
                    .profileImg(requestDto.getProfileImg())
                    .tags(requestDto.getTags())
                    .status(PortfolioStatus.DRAFT)
                    .lastStep(step)
                    .build();
        } else {
            portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포트폴리오입니다."));

            if (!portfolio.getMember().getId().equals(memberId)) {
                throw new IllegalStateException("권한이 없습니다.");
            }

            // 기존 객체가 있다면 여기서 값을 업데이트
            updatePortfolioByStep(portfolio, requestDto, step);
        }

        portfolio.setLastStep(step);
        // 5단계이고 필수 요건 충족 시 발행
        if (step == 5) {
            try {
                validateEssentials(portfolio);
                portfolio.publish();
            } catch (IllegalStateException e) {
                // 필수 요건 미충족 시 DRAFT 유지
            }
        }

        // 명시적으로 저장 후 ID 반환
        return portfolioRepository.save(portfolio).getPortfolioId();
    }

    // 단계별로 포트폴리오 업데이트
    private void updatePortfolioByStep(Portfolio portfolio, PortfolioSaveRequestDto dto, int step) {
        switch (step) {
            case 1 -> {
                if (dto.getSlug() != null) {
                    // 본인의 현재 슬러그와 다른데 이미 DB에 있다면 중복 처리
                    if (portfolioRepository.existsBySlug(dto.getSlug()) && !dto.getSlug().equals(portfolio.getSlug())) {
                        throw new IllegalArgumentException("이미 사용 중인 주소입니다.");
                    }
                    portfolio.updateSlug(dto.getSlug());
                }
                // Step 1: 기본 정보
                if (dto.getCategory() != null)
                    portfolio.setCategory(dto.getCategory());
                if (dto.getSubCategory() != null)
                    portfolio.setSubCategory(dto.getSubCategory());
                if (dto.getProfileImg() != null)
                    portfolio.setProfileImg(dto.getProfileImg());
            }
            case 2 -> {
                // Step 2: 연락처 정보
                if (dto.getEmail() != null)
                    portfolio.setEmail(dto.getEmail());
                if (dto.getPhone() != null)
                    portfolio.setPhone(dto.getPhone());
                if (dto.getLocation() != null)
                    portfolio.setLocation(dto.getLocation());
            }
            case 3 -> {
                // Step 3: 프로젝트 리스트 (교체 로직)
                if (dto.getProjects() != null) {
                    List<Project> projectEntities = dto.getProjects().stream()
                            .map(projectDto -> Project.builder()
                                    .projectName(projectDto.getProjectName())
                                    .projectSummary(projectDto.getProjectSummary())
                                    .projectImg(projectDto.getProjectImg())
                                    .projectLink(projectDto.getProjectLink())
                                    .portfolio(portfolio) // 연관관계 편의 설정
                                    .build())
                            .toList();
                    // 기존 리스트를 비우고 새 리스트를 추가 (orphanRemoval=true 작동)
                    portfolio.updateProjects(projectEntities);
                }
            }
            case 4 -> {
                // Step 4: 소개글
                if (dto.getSummaryIntro() != null)
                    portfolio.setSummaryIntro(dto.getSummaryIntro());

                // 태그 업데이트 (최대 5개 검증)
                if (dto.getTags() != null) {
                    if (dto.getTags().size() > 5) {
                        throw new IllegalArgumentException("태그는 최대 5개까지만 등록 가능합니다.");
                    }

                    portfolio.getTags().clear();
                    portfolio.getTags().addAll(dto.getTags());
                }
            }
            case 5 -> {
                // Step 5: 레이아웃 및 최종 발행
                if (dto.getLayoutType() != null)
                    portfolio.setLayoutType(dto.getLayoutType());
                // status change handled in saveStep main logic
            }
            default -> throw new IllegalArgumentException("잘못된 단계 설정입니다: " + step);
        }
    }

    // 내가 작성한 포트폴리오 리스트 조회
    @Transactional(readOnly = true)
    public List<MyPortfolioListResponseDto> getMyPortfolios(Long memberId) {
        return portfolioRepository.findAllByMemberIdOrderByUpdatedAtDesc(memberId)
                .stream()
                .map(MyPortfolioListResponseDto::fromEntity)
                .toList();
    }

    // 포트폴리오 상세 조회
    @Transactional
    public PortfolioDetailResponseDto getPortfolioDetail(String slug, MemberDetails memberDetails) {
        // 1. ID가 아닌 slug로 조회 (findTopBySlug로 변경)
        Portfolio portfolio = portfolioRepository.findTopBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 명함 주소입니다."));

        Long currentMemberId = (memberDetails != null) ? memberDetails.getMember().getId() : null;
        boolean isOwner = (currentMemberId != null) && portfolio.getMember().getId().equals(currentMemberId);

        // 2. 발행 상태 체크 (비로그인/타인 차단)
        if (portfolio.getStatus() == PortfolioStatus.DRAFT && !isOwner) {
            throw new IllegalStateException("아직 발행되지 않은 명함입니다.");
        }

        // 본인이 아닐 때만 조회수 처리
        if (!isOwner) {
            // 1. 전체 조회수 증가
            portfolio.incrementViewCount(); // (기존 컬럼)

            // 2. 일일 조회수 증가 로직
            LocalDate today = LocalDate.now();
            PortfolioViewLog viewLog = viewLogRepository.findTopByPortfolioAndViewDate(portfolio, today)
                    .orElseGet(() -> viewLogRepository.save(new PortfolioViewLog(portfolio, today)));

            if (viewLog.getId() != null) { // 방금 생성된 것이 아니라면 증가
                viewLog.increment();
            }
        }

        // ResponseDto에 오늘 조회수와 전체 조회수를 모두 담아 반환
        Integer todayCount = viewLogRepository.findTopByPortfolioAndViewDate(portfolio, LocalDate.now())
                .map(PortfolioViewLog::getDailyCount).orElse(0);

        return PortfolioDetailResponseDto.fromEntity(portfolio, isOwner, todayCount);
    }

    // 포트폴리오 공유 링크 생성
    @Transactional(readOnly = true)
    public String generateShareLink(Long memberId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 명함입니다."));

        if (!portfolio.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("본인의 명함 링크만 생성할 수 있습니다.");
        }

        // 무조건 슬러그(랜덤값) 기반으로 링크 생성
        return domain + "/portfolio/" + portfolio.getSlug();
    }

    // 포트폴리오 목록 조회 (카테고리 및 태그별 필터링, 페이지네이션, 정렬)
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PortfolioListResponseDto> getPortfolioList(List<String> categoryStrs,
            List<String> tags,
            String sortStr,
            Pageable pageable) {
        // 정렬 조건 설정
        Sort sort = switch (sortStr) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "updatedAt");
            case "POPULAR" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "REALTIME" -> Sort.by(Sort.Direction.DESC, "todayViewCount");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt"); // LATEST
        };

        // Pageable 객체 재생성 (기존 page, size 유지 + 새로운 sort 적용)
        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort);

        List<OccupationCategory> categories = null;
        if (categoryStrs != null && !categoryStrs.isEmpty()) {
            categories = categoryStrs.stream()
                    .map(OccupationCategory::from)
                    .filter(java.util.Objects::nonNull) // null 제외
                    .toList();

            if (categories.isEmpty()) {
                categories = null; // 유효한 카테고리가 없으면 전체 조회
            }
        }

        org.springframework.data.domain.Page<Portfolio> portfolioPage = portfolioRepository
                .findPublishedPortfolios(categories, tags, sortedPageable);

        return portfolioPage.map(PortfolioListResponseDto::fromEntity);
    }

    // 포트폴리오 삭제 메서드
    @Transactional
    public void deletePortfolio(Long memberId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포트폴리오입니다."));
        if (!portfolio.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        portfolioRepository.delete(portfolio);
    }

    // 포트폴리오 상태 변경 (공개/비공개)
    @Transactional
    public void updateStatus(Long memberId, Long portfolioId, PortfolioStatus status) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포트폴리오입니다."));

        if (!portfolio.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        if (status == PortfolioStatus.PUBLISHED) {
            validateEssentials(portfolio);
        }

        portfolio.setStatus(status);
    }

    // 발행을 위한 필수 데이터 검증
    private void validateEssentials(Portfolio portfolio) {
        // Step 1: 직군, 분야
        if (portfolio.getCategory() == null || portfolio.getSubCategory() == null
                || portfolio.getSubCategory().trim().isEmpty()) {
            throw new IllegalStateException("1단계 필수 항목(직군/분야)이 누락되었습니다.");
        }
        // Step 2: 이메일 (전화번호, 위치, 프로필 이미지는 필수 아님)
        if (portfolio.getEmail() == null || portfolio.getEmail().trim().isEmpty()) {
            throw new IllegalStateException("2단계 필수 항목(이메일)이 누락되었습니다.");
        }
        // Step 5: 레이아웃
        if (portfolio.getLayoutType() == null) {
            throw new IllegalStateException("5단계 필수 항목(레이아웃)이 누락되었습니다.");
        }
    }
}
