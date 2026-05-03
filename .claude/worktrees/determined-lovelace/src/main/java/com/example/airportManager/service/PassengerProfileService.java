package com.example.airportManager.service;

import com.example.airportManager.dto.profile.PassengerProfileResponseDTO;
import com.example.airportManager.dto.profile.PassengerProfileUpdateDTO;

import java.util.UUID;

public interface PassengerProfileService {
    PassengerProfileResponseDTO getMyProfile(UUID userId);
    PassengerProfileResponseDTO updateMyProfile(UUID userId, PassengerProfileUpdateDTO dto);
}
