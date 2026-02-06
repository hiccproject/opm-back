package opm.example.opm.domain.portfolio;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
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

    private final String title;

    OccupationCategory(String title) { this.title = title; }

    public String getTitle() { return title; }

    @JsonCreator
    public static OccupationCategory from(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("string")) {
            return null;
        }

        for (OccupationCategory category : OccupationCategory.values()) {
            // 영문 이름 혹은 한글 타이틀과 일치하는지 확인
            if (category.name().equalsIgnoreCase(value.trim()) ||
                    category.getTitle().equals(value.trim())) {
                return category;
            }
        }
        return null; // 일치하는 게 없으면 에러 대신 null을 반환하여 전체 검색이 되게 함
    }
}