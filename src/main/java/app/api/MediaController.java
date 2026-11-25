package app.api;

import app.api.dto.CreateMediaRequest;
import app.api.dto.MediaResponse;
import app.api.dto.UpdateMediaRequest;
import app.domain.MediaItem;
import app.service.StreamingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final StreamingService streamingService;

    public MediaController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @GetMapping
    public List<MediaResponse> getMediaFiltered(
            @RequestParam(name = "ageRating", required = false) String ageRating,
            @RequestParam(name = "rating", required = false) String rating,
            @RequestParam(name = "onlyFree", required = false) Boolean onlyFree) {
        String effectiveRating = (ageRating != null) ? ageRating : rating;
        return streamingService.getMediaFiltered(effectiveRating, onlyFree).stream()
                .map(MediaResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<MediaResponse> createMedia(@Valid @RequestBody CreateMediaRequest request) {
        boolean isPremium = request.isPremium() != null && request.isPremium();
        MediaItem created = streamingService.createMedia(request.title(), request.ageRating(), isPremium);
        URI location = URI.create("/api/media/" + created.getId());
        return ResponseEntity.created(location).body(MediaResponse.from(created));
    }

    @GetMapping("/{id}")
    public MediaResponse getMedia(@PathVariable String id) {
        return MediaResponse.from(streamingService.getMedia(id));
    }

    @PutMapping("/{id}")
    public MediaResponse updateMedia(@PathVariable String id, @Valid @RequestBody UpdateMediaRequest request) {
        MediaItem updated = streamingService.updateMedia(id, request.title(), request.ageRating(), request.isPremium());
        return MediaResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String id) {
        streamingService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
