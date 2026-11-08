package org.example.Client.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank @Email @Size(max = 100)
        String email,

        @Size(max = 50)
        String name,

        @Size(max = 100)
        String nickname
) {}
