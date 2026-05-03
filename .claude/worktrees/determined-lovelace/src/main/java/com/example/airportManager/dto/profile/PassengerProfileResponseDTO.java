package com.example.airportManager.dto.profile;

import java.util.UUID;

public record PassengerProfileResponseDTO(
        UUID userId,
        String email,
        String phone,
        String docType,
        String docNumber,
        String nationality,
        String loyaltyTier,
        String emergencyContact
) {}
