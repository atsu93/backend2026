package app.repository;

import app.domain.MediaItem;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryMediaRepository implements MediaRepository {

    private final Map<String, MediaItem> storage = new HashMap<>();

    @Override
    public MediaItem save(MediaItem mediaItem) {
        storage.put(mediaItem.getId(), mediaItem);
        return mediaItem;
    }

    @Override
    public Optional<MediaItem> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<MediaItem> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }
}
