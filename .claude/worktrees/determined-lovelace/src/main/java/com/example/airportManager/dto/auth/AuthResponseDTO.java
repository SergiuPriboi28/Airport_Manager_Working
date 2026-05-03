package com.example.airportManager.dto.auth;

import java.util.Set;
import java.util.UUID;

public record AuthResponseDTO(
        UUID userId,
        String email,
        Set<String> roles,
        String token
) {}
