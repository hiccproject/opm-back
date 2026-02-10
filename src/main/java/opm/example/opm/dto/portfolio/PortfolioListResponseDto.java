package opm.example.opm.dto.portfolio;

import opm.example.opm.domain.portfolio.Portfolio;

import java.time.LocalDateTime;
import java.util.List;

// PortfolioListResponseDto.java
public class PortfolioListResponseDto {
    private String slug;
    private String profileImg;
    private String categoryTitle;
    private String subCategory;
    private List<String> tags;
    private LocalDateTime updatedAt;

    // Getter들 직접 작성 (Lombok 없이)
    public String getSlug() {
        return slug;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public PortfolioListResponseDto(String slug, String profileImg, String categoryTitle, String subCategory,
            List<String> tags, LocalDateTime updatedAt, String username) {
        this.slug = slug;
        this.profileImg = profileImg;
        this.categoryTitle = categoryTitle;
        this.subCategory = subCategory;
        this.tags = tags;
        this.updatedAt = updatedAt;
        this.username = username;
    }

    public static PortfolioListResponseDto fromEntity(Portfolio p) {
        if (p == null)
            return null;
        return new PortfolioListResponseDto(
                p.getSlug(), p.getProfileImg(),
                p.getCategory() != null ? p.getCategory().getTitle() : null,
                p.getSubCategory(), p.getTags(), p.getUpdatedAt(),
                p.getMember() != null ? p.getMember().getName() : null);
    }
}