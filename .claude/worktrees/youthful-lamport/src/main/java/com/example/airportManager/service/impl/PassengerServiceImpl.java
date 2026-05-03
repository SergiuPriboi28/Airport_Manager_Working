package com.example.airportManager.service.impl;

import com.example.airportManager.dto.passenger.PassengerCreateDTO;
import com.example.airportManager.dto.passenger.PassengerResponseDTO;
import com.example.airportManager.mapper.PassengerMapper;
import com.example.airportManager.model.Passenger;
import com.example.airportManager.repository.PassengerRepository;
import com.example.airportManager.service.PassengerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository passengerRepository;
    private final PassengerMapper passengerMapper;

    @Override
    @Transactional
    public PassengerResponseDTO create(PassengerCreateDTO dto) {
        Passenger passenger = passengerMapper.toEntity(dto);
        Passenger saved = passengerRepository.save(passenger);
        return passengerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponseDTO getById(Long id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found with id: " + id));
        return passengerMapper.toResponse(passenger);
    }
}
