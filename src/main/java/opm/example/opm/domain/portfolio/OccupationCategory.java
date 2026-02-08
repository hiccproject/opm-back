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

    OccupationCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @JsonCreator
    public static OccupationCategory from(String value) {
        // 아무것도 안 들어오면 무조건 null 리턴해서 전체 검색 모드로 진입
        if (value == null || value.trim().isEmpty())
            return null;

        String cleanValue = value.trim();
        for (OccupationCategory category : OccupationCategory.values()) {
            if (category.name().equalsIgnoreCase(cleanValue) ||
                    category.title.contains(cleanValue)) { // 포함 여부로 변경 (예: "개발" -> "IT·개발")
                return category;
            }
        }
        return null; // 못 찾아도 에러 내지 말고 null 리턴
    }
}