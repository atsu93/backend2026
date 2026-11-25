package app.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpgradeRequest(
        @NotBlank(message = "profileId field is required")
        String profileId
) {
}
