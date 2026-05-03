package com.example.airportManager.dto.booking;

import com.example.airportManager.model.BookingStatus;

import java.util.UUID;

public record BookingResponseDTO(
        Long id,
        String pnr,
        UUID passengerId,
        Long flightId,
        BookingStatus status
) {}
