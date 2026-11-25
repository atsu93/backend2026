package app.api.dto;

import jakarta.validation.constraints.NotBlank;

public record WatchRequest(
        @NotBlank(message = "profileId is required")
        String profileId,

        @NotBlank(message = "mediaId is required")
        String mediaId
) {
}
