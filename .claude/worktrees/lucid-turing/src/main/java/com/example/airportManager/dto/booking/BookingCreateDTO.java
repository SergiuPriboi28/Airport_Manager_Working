package com.example.airportManager.dto.booking;

import jakarta.validation.constraints.NotNull;

public record BookingCreateDTO(
        @NotNull Long flightId
) {}
