package com.example.airportManager.dto.user;

import com.example.airportManager.model.RoleName;
import com.example.airportManager.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Set;

public record UserUpdateDTO(
        @NotBlank @Email String email,
        @NotBlank String phone,
        String password,
        UserStatus status,
        Set<RoleName> roles,
        String position,
        String department,
        String grade,
        LocalDate hireDate,
        String docType,
        String docNumber,
        String nationality,
        String loyaltyTier,
        String emergencyContact
) {}
