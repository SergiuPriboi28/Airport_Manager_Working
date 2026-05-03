package com.example.airportManager.dto.user;

import com.example.airportManager.model.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String phone,
        UserStatus status,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        EmployeeProfileDTO employeeProfile,
        PassengerProfileDTO passengerProfile
) {}
