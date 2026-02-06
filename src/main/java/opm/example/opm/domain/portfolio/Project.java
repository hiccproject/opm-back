package opm.example.opm.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    private String projectName;   // 프젝 이름

    @Column(length = 100)
    private String projectSummary; // 프젝 요약 (100자 이내)

    @Column(length = 500)
    private String projectImg; // 프젝 사진 경로

    private String projectLink;    // 프젝 링크

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio; // 어느 포트폴리오에 속한 프로젝트인지
}