package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;

import java.time.LocalDateTime;

public record FlightFilterDTO(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Long routeId,
        FlightStatus status,
        Integer page,
        Integer size,
        String sort
) {}
