package com.example.airportManager.dto.route;

import com.example.airportManager.validation.DifferentAirports;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@DifferentAirports
public record RouteCreateDTO(
        @NotNull Long originAirportId,
        @NotNull Long destAirportId,
        @Positive int distanceNm,
        @Positive int stdDurationMin
) {}
