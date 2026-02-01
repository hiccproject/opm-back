package opm.example.opm.domain.portfolio;

public enum LayoutType {
    CARD("카드형"),
    GRID("그리드형"),
    LIST("리스트형");

    private final String description;

    LayoutType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
