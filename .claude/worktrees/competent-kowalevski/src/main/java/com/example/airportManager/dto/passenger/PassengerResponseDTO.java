package com.example.airportManager.dto.passenger;

public record PassengerResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String nationality
) {}
