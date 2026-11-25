package app.api.dto;

import app.domain.MediaItem;

public record MediaResponse(
        String id,
        String title,
        String ageRating,
        boolean isPremium,
        int viewsCount
) {
    public static MediaResponse from(MediaItem item) {
        return new MediaResponse(
                item.getId(),
                item.getTitle(),
                item.getAgeRating(),
                item.isPremium(),
                item.getViewsCount()
        );
    }
}
