package com.example.airportManager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank String docType,
        @NotBlank String docNumber,
        @NotBlank String nationality,
        String loyaltyTier,
        String emergencyContact
) {}
