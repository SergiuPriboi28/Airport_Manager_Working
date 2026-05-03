package com.example.airportManager.service;

import com.example.airportManager.dto.profile.EmployeeProfileResponseDTO;
import com.example.airportManager.dto.profile.EmployeeProfileUpdateDTO;

import java.util.UUID;

public interface EmployeeProfileService {
    EmployeeProfileResponseDTO getMyProfile(UUID userId);
    EmployeeProfileResponseDTO updateMyProfile(UUID userId, EmployeeProfileUpdateDTO dto);
}
