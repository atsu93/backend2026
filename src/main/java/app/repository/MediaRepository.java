package app.repository;

import app.domain.MediaItem;

import java.util.List;
import java.util.Optional;

public interface MediaRepository {
    MediaItem save(MediaItem mediaItem);

    Optional<MediaItem> findById(String id);

    List<MediaItem> findAll();

    boolean deleteById(String id);
}
