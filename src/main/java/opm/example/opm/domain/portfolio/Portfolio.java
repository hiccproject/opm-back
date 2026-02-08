package opm.example.opm.domain.portfolio;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.*;
import opm.example.opm.domain.member.Member;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "PORTFOLIOS")
public class Portfolio extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PortfolioId;

    // Step 1: 직군 및 분야
    @Convert(converter = OccupationCategoryConverter.class)
    @Column(nullable = false)
    private OccupationCategory category;
    private String subCategory; // 예: 백엔드, 프론트엔드
    private String profileImg; // 프로필 사진 경로

    // Step 2: 추가 정보
    private String email;
    private String phone; // 선택
    private String location; // 선택

    // Step 3: 프로젝트 리스트 (1:N 관계)
    @Builder.Default // Builder 사용 시 초기화 보장
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    // Step 4: 소개글
    @Column(length = 500)
    private String summaryIntro; // 포폴 요약 소개글

    // Step 5: 형태 선택
    @Enumerated(EnumType.STRING)
    private LayoutType layoutType;

    // 조회수
    @Builder.Default
    @Column(nullable = false)
    private Integer viewCount = 0;

    // 포트폴리오 카드 상태 관리(임시저장)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PortfolioStatus status = PortfolioStatus.DRAFT; // 기본값은 임시 저장 상태

    @Builder.Default
    private Integer lastStep = 0; // 현재 완료한 단계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 태그 리스트
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PORTFOLIO_TAGS", joinColumns = @JoinColumn(name = "portfolio_id"))
    @Column(name = "tag_name")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // 편의 메서드: 프로젝트 리스트 교체
    public void updateProjects(List<Project> newProjects) {
        this.projects.clear();
        if (newProjects != null) {
            newProjects.forEach(p -> {
                p.setPortfolio(this);
                this.projects.add(p);
            });
        }
    }

    public void publish() {
        this.status = PortfolioStatus.PUBLISHED;
    }

    // 슬러그 필드 추가
    @Column(unique = true) // 중복 방지
    private String slug;

    // 업데이트 메서드 추가
    public void updateSlug(String slug) {
        this.slug = slug;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    // 일일 조회수 (정렬용 가상 컬럼)
    @Formula("(SELECT COALESCE(SUM(l.daily_count), 0) FROM portfolio_view_log l WHERE l.portfolio_id = portfolio_id AND l.view_date = CURRENT_DATE)")
    private int todayViewCount;

}
