package app.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserProfile {
    private final String id;
    private String name;
    private SubscriptionType type;
    private double balance;
    private final List<String> watchedMediaIds = new ArrayList<>();

    public UserProfile(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Profile ID cannot be null or blank");
        }
        this.id = id;
        this.name = validateAndNormalizeName(name);
        this.type = SubscriptionType.REGULAR;
        this.balance = 0.0;
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
        this.balance += amount;
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateAndNormalizeName(name);
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
