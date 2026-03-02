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
    private String username;
    private LocalDateTime updatedAt;

    // 좋아요, 스크랩
    private int likeCount;
    private int scrapCount;
    private boolean isLiked;
    private boolean isScraped;

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

    public int getLikeCount() {
        return likeCount;
    }

    public int getScrapCount() {
        return scrapCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public boolean isScraped() {
        return isScraped;
    }

    public PortfolioListResponseDto(String slug, String profileImg, String categoryTitle, String subCategory,
            List<String> tags, LocalDateTime updatedAt, String username,
            int likeCount, int scrapCount, boolean isLiked, boolean isScraped) {
        this.slug = slug;
        this.profileImg = profileImg;
        this.categoryTitle = categoryTitle;
        this.subCategory = subCategory;
        this.tags = tags;
        this.updatedAt = updatedAt;
        this.username = username;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.isLiked = isLiked;
        this.isScraped = isScraped;
    }

    public static PortfolioListResponseDto fromEntity(Portfolio p, boolean isLiked, boolean isScraped) {
        if (p == null)
            return null;
        return new PortfolioListResponseDto(
                p.getSlug(), p.getProfileImg(),
                p.getCategory() != null ? p.getCategory().getTitle() : null,
                p.getSubCategory(), p.getTags(), p.getUpdatedAt(),
                p.getMember() != null ? p.getMember().getName() : null,
                p.getLikeCount(), p.getScrapCount(), isLiked, isScraped);
    }
}