package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FlightFilterDTO(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Long routeId,
        FlightStatus status,
        List<FlightStatus> statuses,
        Integer page,
        Integer size,
        String sort,
        String code,
        String origin,
        String destination,
        Long originId,
        Long destinationId
) {}
