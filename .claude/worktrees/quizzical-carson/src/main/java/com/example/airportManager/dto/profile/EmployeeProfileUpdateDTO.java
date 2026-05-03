package com.example.airportManager.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EmployeeProfileUpdateDTO(
        @NotBlank String position,
        @NotBlank String department,
        String grade,
        @NotNull LocalDate hireDate
) {}
