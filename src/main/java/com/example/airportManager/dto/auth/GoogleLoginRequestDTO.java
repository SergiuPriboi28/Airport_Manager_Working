package com.example.airportManager.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDTO(@NotBlank String idToken) {}