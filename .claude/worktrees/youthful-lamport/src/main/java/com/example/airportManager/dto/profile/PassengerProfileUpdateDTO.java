package com.example.airportManager.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record PassengerProfileUpdateDTO(
        @NotBlank String docType,
        @NotBlank String docNumber,
        @NotBlank String nationality,
        String loyaltyTier,
        String emergencyContact
) {}
