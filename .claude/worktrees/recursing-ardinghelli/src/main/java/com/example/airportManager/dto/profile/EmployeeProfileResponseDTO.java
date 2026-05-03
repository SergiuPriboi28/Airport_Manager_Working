package com.example.airportManager.dto.profile;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeProfileResponseDTO(
        UUID userId,
        String email,
        String phone,
        String position,
        String department,
        String grade,
        LocalDate hireDate
) {}
