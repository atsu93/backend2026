package app.service;

import app.domain.MediaItem;
import app.domain.SubscriptionType;
import app.domain.UserProfile;
import app.repository.MediaRepository;
import app.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StreamingService {
    private final ProfileRepository profileRepository;
    private final MediaRepository mediaRepository;
    private final double premiumPrice;

    public StreamingService(
            ProfileRepository profileRepository,
            MediaRepository mediaRepository,
            @Value("${app.subscription.premium-price:299.0}") double premiumPrice) {
        this.profileRepository = profileRepository;
        this.mediaRepository = mediaRepository;
        this.premiumPrice = premiumPrice;
    }

    public UserProfile createProfile(String name, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be blank");
        }
        if (age < 0 || age > 100) {
            throw new IllegalArgumentException("Age must be between 0 and 100");
        }
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, name, age);
        return profileRepository.save(profile);
    }

    public UserProfile getProfile(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Profile ID cannot be blank");
        }
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + id));
    }

    public List<UserProfile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public UserProfile updateProfile(String id, String name) {
        UserProfile profile = getProfile(id);
        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Profile name cannot be blank");
            }
            profile.setName(name);
            profileRepository.save(profile);
        }
        return profile;
    }

    public void deleteProfile(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Profile ID cannot be blank");
        }
        if (!profileRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Profile not found: " + id);
        }
    }

    public void topUpBalance(String profileId, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive finite number");
        }
        UserProfile profile = getProfile(profileId);
        if (Double.isInfinite(profile.getBalance() + amount)) {
            throw new IllegalArgumentException("Balance overflow");
        }
        profile.addMoney(amount);
        profileRepository.save(profile);
    }

    public void buyPremium(String profileId) {
        UserProfile profile = getProfile(profileId);
        if (profile.getType() == SubscriptionType.PREMIUM) {
            throw new BusinessConflictException("You already have Premium status");
        }
        if (profile.getBalance() < premiumPrice) {
            throw new IllegalArgumentException("Insufficient funds to buy Premium");
        }
        profile.spendMoney(premiumPrice);
        profile.setType(SubscriptionType.PREMIUM);
        profileRepository.save(profile);
    }

    public MediaItem createMedia(String title, String ageRating, boolean isPremium) {
        String id = UUID.randomUUID().toString();
        MediaItem item = new MediaItem(id, title, ageRating, isPremium);
        return mediaRepository.save(item);
    }

    public MediaItem getMedia(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Media ID cannot be blank");
        }
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media item not found: " + id));
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
            return mediaRepository.save(item);
        } catch (IllegalArgumentException e) {
            item.setTitle(oldTitle);
            item.setAgeRating(oldAgeRating);
            throw e;
        }
    }

    public void deleteMedia(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Media ID cannot be blank");
        }
        if (!mediaRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Media item not found: " + id);
        }
    }

    public List<MediaItem> getMediaFiltered(String ageRating, Boolean onlyFree) {
        return mediaRepository.findAll().stream()
                .filter(m -> ageRating == null || ageRating.isBlank()
                        || m.getAgeRating().equalsIgnoreCase(ageRating.trim()))
                .filter(m -> onlyFree == null || !onlyFree == m.isPremium())
                .toList();
    }

    public void watchMedia(String profileId, String mediaId) {
        UserProfile profile = getProfile(profileId);
        MediaItem media = getMedia(mediaId);

        if (!profile.getType().canWatch(media)) {
            throw new MediaAccessDeniedException("Access denied: Premium subscription required");
        }
        if (!profile.canWatchByAge(media)) {
            throw new MediaAccessDeniedException("Access denied: age restrictions apply");
        }

        profile.addWatchedMedia(mediaId);
        media.incrementViews();

        profileRepository.save(profile);
        mediaRepository.save(media);
    }
}
