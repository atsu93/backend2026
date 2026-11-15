package app.domain;

import java.util.Objects;
import java.util.Set;

public enum SubscriptionType {
    REGULAR {
        private static final Set<String> RESTRICTED_RATINGS = Set.of("R", "NC-17", "18+");

        @Override
        public boolean canWatch(MediaItem media) {
            Objects.requireNonNull(media, "Media item cannot be null");
            if (media.isPremium()) {
                return false;
            }
            String rating = media.getAgeRating();
            if (rating == null) {
                return false;
            }
            return !RESTRICTED_RATINGS.contains(rating.trim().toUpperCase());
        }
    },

    PREMIUM {
        @Override
        public boolean canWatch(MediaItem media) {
            Objects.requireNonNull(media, "Media item cannot be null");
            return true;
        }
    };

    public abstract boolean canWatch(MediaItem media);
}
