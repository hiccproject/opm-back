package opm.example.opm.service;


import lombok.RequiredArgsConstructor;
import opm.example.opm.common.oauth.MemberDetails;
import opm.example.opm.domain.member.Member;
import opm.example.opm.domain.portfolio.Portfolio;
import opm.example.opm.domain.portfolio.PortfolioStatus;
import opm.example.opm.domain.portfolio.PortfolioViewLog;
import opm.example.opm.domain.portfolio.Project;
import opm.example.opm.dto.portfolio.MyPortfolioListResponseDto;
import opm.example.opm.dto.portfolio.PortfolioDetailResponseDto;
import opm.example.opm.dto.portfolio.PortfolioSaveRequestDto;
import opm.example.opm.repository.MemberRepository;
import opm.example.opm.repository.PortfolioRepository;
import opm.example.opm.repository.PortfolioViewLogRepository;
import org.springframework.beans.factory.annotation.Value;
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
        // 5단계일 때 발행 상태 변경
        if (step == 5) {
            portfolio.publish();
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
                if (dto.getCategory() != null) portfolio.setCategory(dto.getCategory());
                if (dto.getSubCategory() != null) portfolio.setSubCategory(dto.getSubCategory());
                if (dto.getProfileImg() != null) portfolio.setProfileImg(dto.getProfileImg());
            }
            case 2 -> {
                // Step 2: 연락처 정보
                if (dto.getEmail() != null) portfolio.setEmail(dto.getEmail());
                if (dto.getPhone() != null) portfolio.setPhone(dto.getPhone());
                if (dto.getLocation() != null) portfolio.setLocation(dto.getLocation());
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
                if (dto.getSummaryIntro() != null) portfolio.setSummaryIntro(dto.getSummaryIntro());
            }
            case 5 -> {
                // Step 5: 레이아웃 및 최종 발행
                if (dto.getLayoutType() != null) portfolio.setLayoutType(dto.getLayoutType());
                portfolio.publish(); // status = PUBLISHED
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
        // 1. ID가 아닌 slug로 조회
        Portfolio portfolio = portfolioRepository.findBySlug(slug)
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
            PortfolioViewLog viewLog = viewLogRepository.findByPortfolioAndViewDate(portfolio, today)
                    .orElseGet(() -> viewLogRepository.save(new PortfolioViewLog(portfolio, today)));

            if (viewLog.getId() != null) { // 방금 생성된 것이 아니라면 증가
                viewLog.increment();
            }
        }

        // ResponseDto에 오늘 조회수와 전체 조회수를 모두 담아 반환
        Integer todayCount = viewLogRepository.findByPortfolioAndViewDate(portfolio, LocalDate.now())
                .map(PortfolioViewLog::getDailyCount).orElse(0);

        return PortfolioDetailResponseDto.fromEntity(portfolio, isOwner, todayCount);
    }

    // 포트폴리오 공유 링크 생성
    @Transactional(readOnly = true)
    public String generateShareLink(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 명함입니다."));

        // 무조건 슬러그(랜덤값) 기반으로 링크 생성
        return domain + "/portfolio/" + portfolio.getSlug();
    }
}
