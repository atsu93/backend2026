package app.domain;

import java.util.*;

public class UserProfile {
    private final String id;
    private String name;
    private int age;
    private SubscriptionType type;
    private double balance;
    private final List<String> watchedMediaIds = new ArrayList<>();

    public UserProfile(String id, String name, int age) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Profile ID cannot be null or blank");
        }
        this.id = id;
        this.name = validateAndNormalizeName(name);
        this.age = validateAge(age);
        this.type = SubscriptionType.REGULAR;
        this.balance = 0.0;
    }

    private static final Set<String> RESTRICTED_RATINGS = Set.of("R", "NC-17", "18+");

    public boolean canWatchByAge(MediaItem media) {
        Objects.requireNonNull(media, "Media item cannot be null");
        String rating = media.getAgeRating();
        if (rating == null) {
            return false;
        }
        if (RESTRICTED_RATINGS.contains(rating.trim().toUpperCase())) {
            return this.age >= 18;
        }
        return true;
    }

    public void addWatchedMedia(String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            throw new IllegalArgumentException("Media ID cannot be null or blank");
        }
        if (!watchedMediaIds.contains(mediaId)) {
            watchedMediaIds.add(mediaId);
        }
    }

    public void addMoney(double amount) {
        validateAmount(amount);
        double newBalance = this.balance + amount;
        if (Double.isInfinite(newBalance)) {
            throw new IllegalArgumentException("Balance overflow");
        }
        this.balance = newBalance;
    }

    public void spendMoney(double amount) {
        validateAmount(amount);
        if (this.balance < amount) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance -= amount;
    }

    private void validateAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive finite number");
        }
    }

    private String validateAndNormalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be null or blank");
        }
        return name.trim();
    }

    private int validateAge(int age) {
        if (age < 0 || age > 100) {
            throw new IllegalArgumentException("Age must be between 0 and 100");
        }
        return age;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateAndNormalizeName(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = validateAge(age);
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = Objects.requireNonNull(type, "Subscription type cannot be null");
    }

    public List<String> getWatchedMediaIds() {
        return Collections.unmodifiableList(watchedMediaIds);
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserProfile that = (UserProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
