package app.domain;

import java.util.Objects;
import java.util.Set;

public class MediaItem {
    private static final Set<String> VALID_RATINGS = Set.of(
            "G", "PG", "PG-13", "R", "NC-17",
            "0+", "6+", "12+", "16+", "18+"
    );

    private final String id;
    private String title;
    private String ageRating;
    private boolean isPremium;
    private int viewsCount = 0;

    public MediaItem(String id, String title, String ageRating, boolean isPremium) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Media ID cannot be null or blank");
        }

        this.id = id;
        this.title = validateTitle(title);
        this.ageRating = validateAndNormalizeRating(ageRating);
        this.isPremium = isPremium;
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Media title cannot be null or blank");
        }
        return title.trim();
    }

    public static String validateAndNormalizeRating(String ageRating) {
        if (ageRating == null || ageRating.isBlank()) {
            throw new IllegalArgumentException("Age rating cannot be null or blank");
        }
        String normalized = ageRating.trim().toUpperCase();
        if (!VALID_RATINGS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported age rating: " + ageRating);
        }
        return normalized;
    }

    public void incrementViews() {
        viewsCount++;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = validateTitle(title);
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = validateAndNormalizeRating(ageRating);
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaItem mediaItem = (MediaItem) o;
        return Objects.equals(id, mediaItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
