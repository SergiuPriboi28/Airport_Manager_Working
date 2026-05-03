package com.example.airportManager.dto.user;

public record PassengerProfileDTO(
        String docType,
        String docNumber,
        String nationality,
        String loyaltyTier,
        String emergencyContact
) {}
