package com.example.airportManager.dto.route;

public record RouteResponseDTO(
        Long id,
        Long originAirportId,
        Long destAirportId,
        int distanceNm,
        int stdDurationMin
) {}
