package com.example.airportManager.validation;

import com.example.airportManager.dto.route.RouteCreateDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentAirportsValidator implements ConstraintValidator<DifferentAirports, RouteCreateDTO> {

    @Override
    public boolean isValid(RouteCreateDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        if (dto.originAirportId() == null || dto.destAirportId() == null) {
            return true;
        }
        return !dto.originAirportId().equals(dto.destAirportId());
    }
}
