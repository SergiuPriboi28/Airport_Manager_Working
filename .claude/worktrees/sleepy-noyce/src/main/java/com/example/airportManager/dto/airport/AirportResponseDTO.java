package com.example.airportManager.dto.airport;

public record AirportResponseDTO(
        Long id,
        String iata,
        String icao,
        String name,
        String city,
        String country,
        String timezone
) {}
