package app.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMediaRequest(
        @NotBlank(message = "Title cannot be empty")
        String title,

        @NotBlank(message = "ageRating cannot be empty")
        String ageRating,

        Boolean isPremium
) {
}
