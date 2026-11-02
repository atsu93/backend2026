package app.service;

import app.domain.MediaItem;
import app.domain.UserProfile;
import app.domain.ProfileType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StreamingService {
    private final Map<String, UserProfile> profiles = new ConcurrentHashMap<>();
    private final Map<String, MediaItem> mediaItems = new ConcurrentHashMap<>();

    public UserProfile createProfile(String name, ProfileType type){
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, name, type);
        profiles.put(id, profile);
        return profile;
    }

    public UserProfile getProfile(String id){
        UserProfile profile = profiles.get(id);
        if (profile==null) throw new StreamingException("Profile not found", 404);
        return profile;
    }

    public List<UserProfile> getAllProfiles(){
        return  new ArrayList<>(profiles.values());
    }

    public UserProfile updateProfile(String id, String name, ProfileType type){
        UserProfile profile = getProfile(id);
        if (name != null) profile.setName(name);
        if (type != null) profile.setType(type);
        return profile;
    }

    public void deleteProfile(String id){
        if (!profiles.containsKey(id)) throw new StreamingException("Profile not found", 404);
        profiles.remove(id);
    }


    public MediaItem createMedia(String title, String ageRating, boolean isPremium){
        String id = UUID.randomUUID().toString();
        MediaItem item = new MediaItem(id, title, ageRating, isPremium);
        mediaItems.put(id, item);
        return item;
    }

    public MediaItem getMedia(String id) {
        MediaItem item = mediaItems.get(id);
        if (item == null) throw new StreamingException("Media item not found", 404);
        return item;
    }

    public MediaItem updateMedia(String id, String title, String ageRating, Boolean isPremium) {
        MediaItem item = getMedia(id);
        if (title != null) item.setTitle(title);
        if (ageRating != null) item.setAgeRating(ageRating);
        if (isPremium != null) item.setPremium(isPremium);
        return item;
    }

    public void deleteMedia(String id) {
        if (!mediaItems.containsKey(id)) throw new StreamingException("Media item not found", 404);
        mediaItems.remove(id);
    }


    public List<MediaItem> getMediaFiltered(String ageRating, Boolean onlyFree){
        return mediaItems.values().stream()
                .filter(m -> ageRating==null || m.getAgeRating().equalsIgnoreCase(ageRating))
                .filter(m -> onlyFree == null || !onlyFree || !m.isPremium())
                .collect(Collectors.toList());
    }


    public synchronized void watchMedia(String profileId, String mediaId){
        UserProfile profile = getProfile(profileId);
        MediaItem media = getMedia(mediaId);

        if (!profile.getType().canWatch(media.getAgeRating(), media.isPremium())){
            throw new StreamingException("Access denied: profile restrictions apply for this content", 400);
        }

        profile.addWatchedMedia(mediaId);
        media.incrementViews();
    }

    public synchronized void buyPremium(String profileId) {
        UserProfile profile = getProfile(profileId);
        double premiumPrice = 299.0;

        if (profile.getType() == ProfileType.ADULT) {
            throw new StreamingException("You already have Premium status", 400);
        }
        if (profile.getBalance() < premiumPrice) {
            throw new StreamingException("Insufficient funds to buy Premium", 400);
        }

        profile.spendMoney(premiumPrice);
        profile.setType(ProfileType.ADULT);
    }

}
