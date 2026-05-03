package com.example.airportManager.dto.passenger;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PassengerCreateDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String docType,
        @NotBlank String docNumber,
        @NotBlank String nationality
) {}
