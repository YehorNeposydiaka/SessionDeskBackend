package org.example.Client.DTO;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String email,
        String name,
        String nickname
) {}
