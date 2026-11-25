package app.api.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateMediaRequest(
        @Pattern(regexp = ".*\\S.*", message = "Title cannot be blank")
        String title,

        @Pattern(regexp = ".*\\S.*", message = "Age rating cannot be blank")
        String ageRating,

        Boolean isPremium
) {
}
