package app.service;

import app.domain.MediaItem;
import app.domain.SubscriptionType;
import app.domain.UserProfile;

import java.util.*;

public class StreamingService {
    private static final double PREMIUM_PRICE = 299.0;

    private final Map<String, UserProfile> profiles = new HashMap<>();
    private final Map<String, MediaItem> mediaItems = new HashMap<>();

    public UserProfile createProfile(String name, int age) {
        if (name == null || name.isBlank()) {
            throw new StreamingException("Profile name cannot be blank", 400);
        }
        if (age < 0 || age > 100) {
            throw new StreamingException("Age must be between 0 and 120", 400);
        }
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, name, age);
        profiles.put(id, profile);
        return profile;
    }

    public UserProfile getProfile(String id) {
        if (id == null || id.isBlank()) {
            throw new StreamingException("Profile ID cannot be blank", 400);
        }
        UserProfile profile = profiles.get(id);
        if (profile == null) {
            throw new StreamingException("Profile not found", 404);
        }
        return profile;
    }

    public List<UserProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

    public UserProfile updateProfile(String id, String name) {
        UserProfile profile = getProfile(id);
        if (name != null) {
            if (name.isBlank()) {
                throw new StreamingException("Profile name cannot be blank", 400);
            }
            profile.setName(name);
        }
        return profile;
    }

    public void deleteProfile(String id) {
        if (id == null || id.isBlank()) {
            throw new StreamingException("Profile ID cannot be blank", 400);
        }
        if (profiles.remove(id) == null) {
            throw new StreamingException("Profile not found", 404);
        }
    }

    public void topUpBalance(String profileId, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new StreamingException("Amount must be a positive finite number", 400);
        }
        UserProfile profile = getProfile(profileId);
        if (Double.isInfinite(profile.getBalance() + amount)) {
            throw new StreamingException("Balance overflow", 400);
        }
        profile.addMoney(amount);
    }

    public MediaItem createMedia(String title, String ageRating, boolean isPremium) {
        try {
            String id = UUID.randomUUID().toString();
            MediaItem item = new MediaItem(id, title, ageRating, isPremium);
            mediaItems.put(id, item);
            return item;
        } catch (IllegalArgumentException e) {
            throw new StreamingException(e.getMessage(), 400);
        }
    }

    public MediaItem getMedia(String id) {
        if (id == null || id.isBlank()) {
            throw new StreamingException("Media ID cannot be blank", 400);
        }
        MediaItem item = mediaItems.get(id);
        if (item == null) {
            throw new StreamingException("Media item not found", 404);
        }
        return item;
    }

    public MediaItem updateMedia(String id, String title, String ageRating, Boolean isPremium) {
        MediaItem item = getMedia(id);
        String oldTitle = item.getTitle();
        String oldAgeRating = item.getAgeRating();
        try {
            if (title != null) {
                item.setTitle(title);
            }
            if (ageRating != null) {
                item.setAgeRating(ageRating);
            }
            if (isPremium != null) {
                item.setPremium(isPremium);
            }
            return item;
        } catch (IllegalArgumentException e) {
            item.setTitle(oldTitle);
            item.setAgeRating(oldAgeRating);
            throw new StreamingException(e.getMessage(), 400);
        }
    }

    public void deleteMedia(String id) {
        if (id == null || id.isBlank()) {
            throw new StreamingException("Media ID cannot be blank", 400);
        }
        if (mediaItems.remove(id) == null) {
            throw new StreamingException("Media item not found", 404);
        }
    }

    public List<MediaItem> getMediaFiltered(String ageRating, Boolean onlyFree) {
        return mediaItems.values().stream()
                .filter(m -> ageRating == null || ageRating.isBlank()
                        || m.getAgeRating().equalsIgnoreCase(ageRating.trim()))
                .filter(m -> onlyFree == null || !onlyFree == m.isPremium())
                .toList();
    }

    public void watchMedia(String profileId, String mediaId) {
        UserProfile profile = getProfile(profileId);
        MediaItem media = getMedia(mediaId);

        if (!profile.getType().canWatch(media)) {
            throw new StreamingException("Access denied: subscription or age restrictions apply", 403);
        }
        if (!profile.canWatchByAge(media)) {
            throw new StreamingException("Access denied: age restrictions apply", 403);
        }

        profile.addWatchedMedia(mediaId);
        media.incrementViews();
    }

    public void buyPremium(String profileId) {
        UserProfile profile = getProfile(profileId);
        if (profile.getType() == SubscriptionType.PREMIUM) {
            throw new StreamingException("You already have Premium status", 409);
        }
        if (profile.getBalance() < PREMIUM_PRICE) {
            throw new StreamingException("Insufficient funds to buy Premium", 400);
        }
        profile.spendMoney(PREMIUM_PRICE);
        profile.setType(SubscriptionType.PREMIUM);
    }
}
