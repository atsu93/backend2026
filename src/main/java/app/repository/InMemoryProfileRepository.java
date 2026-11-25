package app.repository;

import app.domain.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryProfileRepository implements ProfileRepository {

    private final Map<String, UserProfile> storage = new HashMap<>();

    @Override
    public UserProfile save(UserProfile profile) {
        storage.put(profile.getId(), profile);
        return profile;
    }

    @Override
    public Optional<UserProfile> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<UserProfile> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }
}
