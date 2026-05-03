package com.example.airportManager.dto.aircraft;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AircraftCreateDTO(
        @NotBlank String tailNumber,
        @NotBlank String model,
        @NotNull @Min(1) Integer capacity,
        String seatMapRef,
        String status
) {}
