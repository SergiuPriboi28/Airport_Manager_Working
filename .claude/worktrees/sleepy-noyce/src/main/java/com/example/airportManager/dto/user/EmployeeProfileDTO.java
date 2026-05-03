package com.example.airportManager.dto.user;

import java.time.LocalDate;

public record EmployeeProfileDTO(
        String position,
        String department,
        String grade,
        LocalDate hireDate
) {}
