package com.example.airportManager.dto.flight;

import com.example.airportManager.model.FlightStatus;
import com.example.airportManager.validation.ArrivalAfterDeparture;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@ArrivalAfterDeparture
public record FlightCreateDTO(
        @NotBlank String code,
        @NotNull Long routeId,
        @NotNull LocalDateTime departureTime,
        @NotNull LocalDateTime arrivalTime,
        @NotNull FlightStatus status
) {}
