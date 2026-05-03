package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FlightDetailResponseDTO(
        Long id,
        String code,
        Long routeId,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        FlightStatus status,
        String gate,
        List<PassengerInfoDTO> passengers
) {}
