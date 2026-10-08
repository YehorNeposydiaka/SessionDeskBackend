package org.example.Specialist.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateSpecialistRequest(

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100,
                message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Size(max = 1000,
                message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 500,
                message = "Avatar URL must not exceed 500 characters")
        String avatarUrl,

        @Size(max = 500,
                message = "Payment details must not exceed 500 characters")
        String paymentDetails,

        @Pattern(
                regexp = "^$|^UA\\d{27}$",
                message = "IBAN must be a valid Ukrainian IBAN"
        )
        String iban,

        @NotBlank(message = "Slug is required")
        @Size(min = 3, max = 100,
                message = "Slug must be between 3 and 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug may contain only lowercase letters, digits and hyphens"
        )
        String slug
) {
}