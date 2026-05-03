package com.example.airportManager.dto.flight;

import java.util.UUID;

public record PassengerInfoDTO(
        UUID userId,
        String email,
        String docType,
        String docNumber,
        String nationality,
        String pnr,
        String bookingStatus
) {}
