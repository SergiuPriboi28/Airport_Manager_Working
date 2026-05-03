package com.example.airportManager.dto.user;

import com.example.airportManager.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UserCreateDTO(
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotNull Set<RoleName> roles,
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
