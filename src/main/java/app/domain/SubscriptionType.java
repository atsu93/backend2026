package app.domain;

import java.util.Objects;

public enum SubscriptionType {
    REGULAR {
        @Override
        public boolean canWatch(MediaItem media) {
            Objects.requireNonNull(media, "Media item cannot be null");
            return !media.isPremium();
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
