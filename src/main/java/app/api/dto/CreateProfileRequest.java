package app.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProfileRequest(
        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be between 0 and 100")
        @Max(value = 100, message = "Age must be between 0 and 100")
        Integer age
) {
}
