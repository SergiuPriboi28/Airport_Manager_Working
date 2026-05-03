package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;

import java.time.LocalDateTime;

public record FlightResponseDTO(
        Long id,
        String code,
        Long routeId,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        FlightStatus status
) {}
