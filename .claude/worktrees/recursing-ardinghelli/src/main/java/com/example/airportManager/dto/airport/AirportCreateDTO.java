package com.example.airportManager.dto.airport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AirportCreateDTO(
        @NotBlank @Size(min = 3, max = 3) String iata,
        @NotBlank @Size(min = 4, max = 4) String icao,
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String country,
        @NotBlank String timezone
) {}
