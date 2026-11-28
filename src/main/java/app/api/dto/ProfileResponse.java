package app.api.dto;

import app.domain.SubscriptionType;
import app.domain.UserProfile;

import java.util.List;

public record ProfileResponse(
        String id,
        String name,
        int age,
        SubscriptionType type,
        double balance,
        List<String> watchedMediaIds
) {
    public static ProfileResponse from(UserProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getAge(),
                profile.getType(),
                profile.getBalance(),
                profile.getWatchedMediaIds()
        );
    }
}
