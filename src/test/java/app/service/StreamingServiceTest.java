package app.service;

import app.domain.MediaItem;
import app.domain.SubscriptionType;
import app.domain.UserProfile;
import app.repository.InMemoryMediaRepository;
import app.repository.InMemoryProfileRepository;
import app.repository.MediaRepository;
import app.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamingServiceTest {

    private ProfileRepository profileRepository;
    private MediaRepository mediaRepository;
    private StreamingService service;

    @BeforeEach
    void setUp() {
        profileRepository = new InMemoryProfileRepository();
        mediaRepository = new InMemoryMediaRepository();
        service = new StreamingService(profileRepository, mediaRepository, 299.0);
    }


    @Test
    void createAndGetProfileSuccess() {
        UserProfile created = service.createProfile("Alice", 25);

        assertNotNull(created.getId());
        assertEquals("Alice", created.getName());
        assertEquals(25, created.getAge());
        assertEquals(SubscriptionType.REGULAR, created.getType());
        assertEquals(0.0, created.getBalance());

        UserProfile found = service.getProfile(created.getId());
        assertEquals(created, found);
    }

    @Test
    void updateProfileChangesNameInRepository() {
        UserProfile created = service.createProfile("Alice", 25);

        UserProfile updated = service.updateProfile(created.getId(), "Alicia");

        assertEquals("Alicia", updated.getName());
        assertEquals("Alicia", profileRepository.findById(created.getId()).orElseThrow().getName());
    }

    @Test
    void deleteProfileRemovesItFromRepository() {
        UserProfile created = service.createProfile("Alice", 25);

        service.deleteProfile(created.getId());

        assertTrue(profileRepository.findById(created.getId()).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> service.getProfile(created.getId()));
    }

    @Test
    void topUpAndBuyPremiumChangesBalanceAndSubscriptionType() {
        UserProfile profile = service.createProfile("Bob", 30);

        service.topUpBalance(profile.getId(), 500.0);
        assertEquals(500.0, service.getProfile(profile.getId()).getBalance());

        service.buyPremium(profile.getId());

        UserProfile updated = service.getProfile(profile.getId());
        assertEquals(SubscriptionType.PREMIUM, updated.getType());
        assertEquals(201.0, updated.getBalance(), 0.001);
    }

    @Test
    void watchMediaIncrementsViewsAndAddsToHistoryWithoutDuplicates() {
        UserProfile profile = service.createProfile("Charlie", 20);
        MediaItem movie = service.createMedia("Inception", "PG-13", false);

        service.watchMedia(profile.getId(), movie.getId());
        service.watchMedia(profile.getId(), movie.getId());

        MediaItem updatedMovie = service.getMedia(movie.getId());
        UserProfile updatedProfile = service.getProfile(profile.getId());

        assertEquals(2, updatedMovie.getViewsCount());
        assertEquals(List.of(movie.getId()), updatedProfile.getWatchedMediaIds());
    }

    @Test
    void getMediaFilteredByRatingAndFreeFlag() {
        service.createMedia("SpongeBob", "G", false);
        service.createMedia("Stranger Things", "PG-13", false);
        service.createMedia("The Witcher", "R", true);

        List<MediaItem> onlyFree = service.getMediaFiltered(null, true);
        assertEquals(2, onlyFree.size());

        List<MediaItem> ratedR = service.getMediaFiltered("R", null);
        assertEquals(1, ratedR.size());
        assertEquals("The Witcher", ratedR.getFirst().getTitle());
    }


    @Test
    void createProfileBoundaryAgesZeroAndHundred() {
        UserProfile baby = service.createProfile("Baby", 0);
        UserProfile elder = service.createProfile("Elder", 100);

        assertEquals(0, baby.getAge());
        assertEquals(100, elder.getAge());
    }

    @Test
    void watchMediaBoundaryExactMinimumAgeAllowed() {
        UserProfile teen = service.createProfile("Teen", 12);
        MediaItem pg13Movie = service.createMedia("Spider-Man", "PG-13", false);

        service.watchMedia(teen.getId(), pg13Movie.getId());

        assertEquals(1, service.getMedia(pg13Movie.getId()).getViewsCount());
    }

    @Test
    void buyPremiumExactPriceLeavesZeroBalance() {
        UserProfile profile = service.createProfile("Exact", 22);
        service.topUpBalance(profile.getId(), 299.0);

        service.buyPremium(profile.getId());

        UserProfile updated = service.getProfile(profile.getId());
        assertEquals(SubscriptionType.PREMIUM, updated.getType());
        assertEquals(0.0, updated.getBalance(), 0.001);
    }


    @Test
    void createProfileInvalidInputsThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.createProfile("   ", 20));
        assertThrows(IllegalArgumentException.class, () -> service.createProfile("Alex", -1));
        assertThrows(IllegalArgumentException.class, () -> service.createProfile("Alex", 101));
    }

    @Test
    void getUnknownResourcesThrowNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.getProfile("unknown-id"));
        assertThrows(ResourceNotFoundException.class, () -> service.getMedia("unknown-id"));
        assertThrows(ResourceNotFoundException.class, () -> service.deleteProfile("unknown-id"));
        assertThrows(ResourceNotFoundException.class, () -> service.deleteMedia("unknown-id"));
    }

    @Test
    void watchPremiumMediaWithFreeAccountThrowsAccessDenied() {
        UserProfile freeUser = service.createProfile("FreeUser", 25);
        MediaItem premiumMovie = service.createMedia("The Witcher", "R", true);

        assertThrows(MediaAccessDeniedException.class,
                () -> service.watchMedia(freeUser.getId(), premiumMovie.getId()));
        assertEquals(0, service.getMedia(premiumMovie.getId()).getViewsCount());
    }

    @Test
    void watchAgeRestrictedMediaByUnderageUserThrowsAccessDenied() {
        UserProfile kid = service.createProfile("Kid", 11);
        MediaItem pg13Movie = service.createMedia("Adult film", "R", false);

        assertThrows(MediaAccessDeniedException.class,
                () -> service.watchMedia(kid.getId(), pg13Movie.getId()));
    }

    @Test
    void buyPremiumInsufficientFundsOrDuplicatePurchaseThrowsExceptions() {
        UserProfile user = service.createProfile("User", 20);
        service.topUpBalance(user.getId(), 100.0);

        assertThrows(IllegalArgumentException.class, () -> service.buyPremium(user.getId()));

        service.topUpBalance(user.getId(), 200.0);
        service.buyPremium(user.getId());

        assertThrows(BusinessConflictException.class, () -> service.buyPremium(user.getId()));
    }

    @Test
    void updateMediaRollsBackTitleWhenRatingIsInvalid() {
        MediaItem media = service.createMedia("Original Title", "G", false);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateMedia(media.getId(), "New Title", "INVALID_RATING", null));

        MediaItem unchanged = service.getMedia(media.getId());
        assertEquals("Original Title", unchanged.getTitle());
        assertEquals("G", unchanged.getAgeRating());
    }

}
