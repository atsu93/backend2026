package app.api.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank")
        String name
) {
}
