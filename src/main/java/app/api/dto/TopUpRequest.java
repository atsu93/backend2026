package app.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TopUpRequest(
        @NotBlank(message = "profileId field is required")
        String profileId,

        @NotNull(message = "amount field is required")
        @Positive(message = "amount must be a positive number")
        Double amount
) {
}
