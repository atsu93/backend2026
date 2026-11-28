package app.api;

import app.api.dto.CreateProfileRequest;
import app.api.dto.ProfileResponse;
import app.api.dto.UpdateProfileRequest;
import app.domain.UserProfile;
import app.service.StreamingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final StreamingService streamingService;

    public ProfileController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @GetMapping
    public List<ProfileResponse> getAllProfiles() {
        return streamingService.getAllProfiles().stream()
                .map(ProfileResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        UserProfile created = streamingService.createProfile(request.name(), request.age());
        URI location = URI.create("/api/profiles/" + created.getId());
        return ResponseEntity.created(location).body(ProfileResponse.from(created));
    }

    @GetMapping("/{id}")
    public ProfileResponse getProfile(@PathVariable String id) {
        return ProfileResponse.from(streamingService.getProfile(id));
    }

    @PutMapping("/{id}")
    public ProfileResponse updateProfile(@PathVariable String id, @Valid @RequestBody UpdateProfileRequest request) {
        UserProfile updated = streamingService.updateProfile(id, request.name());
        return ProfileResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        streamingService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
