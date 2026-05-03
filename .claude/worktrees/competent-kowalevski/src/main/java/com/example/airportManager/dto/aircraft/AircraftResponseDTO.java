package com.example.airportManager.dto.aircraft;

public record AircraftResponseDTO(
        Long id,
        String tailNumber,
        String model,
        int capacity,
        String seatMapRef,
        String status
) {}
