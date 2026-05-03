package com.example.airportManager.service;

import com.example.airportManager.dto.passenger.PassengerCreateDTO;
import com.example.airportManager.dto.passenger.PassengerResponseDTO;

public interface PassengerService {
    PassengerResponseDTO create(PassengerCreateDTO dto);
    PassengerResponseDTO getById(Long id);
}
