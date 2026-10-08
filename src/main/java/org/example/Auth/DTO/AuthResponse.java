package org.example.Auth.DTO;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}