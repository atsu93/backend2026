package app.repository;

import app.domain.UserProfile;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository {
    UserProfile save(UserProfile profile);

    Optional<UserProfile> findById(String id);

    List<UserProfile> findAll();

    boolean deleteById(String id);
}
