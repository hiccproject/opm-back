package opm.example.opm.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OccupationCategory {
    DEVELOPMENT("IT·개발"),
    DESIGN("디자인"),
    MARKETING("마케팅·광고"),
    PLANNING("기획·전략"),
    BUSINESS("영업·고객상담"),
    MANAGEMENT("경영·인사·총무"),
    FINANCE("금융·재무"),
    SERVICE("서비스·교육"),
    ENGINEERING("엔지니어링·설계"),
    MEDIA("미디어·예술"),
    MEDICAL("의료·바이오"),
    OTHERS("기타");

    private final String description;
}