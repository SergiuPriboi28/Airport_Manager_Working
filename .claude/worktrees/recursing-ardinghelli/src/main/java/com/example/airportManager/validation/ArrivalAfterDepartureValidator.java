package com.example.airportManager.validation;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArrivalAfterDepartureValidator implements ConstraintValidator<ArrivalAfterDeparture, FlightCreateDTO> {

    @Override
    public boolean isValid(FlightCreateDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        if (dto.departureTime() == null || dto.arrivalTime() == null) {
            return true;
        }
        return dto.arrivalTime().isAfter(dto.departureTime());
    }
}
