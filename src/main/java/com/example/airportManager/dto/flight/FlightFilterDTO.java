package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FlightFilterDTO(
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        Long routeId,
        FlightStatus status,
        Integer page,
        Integer size,
        String sort,
        String code,
        String origin,
        String destination,
        List<FlightStatus> statuses,
        Long originId,
        Long destinationId
) {}
