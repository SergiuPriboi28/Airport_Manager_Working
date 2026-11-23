package com.example.airportManager.service;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightFilterDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.dto.flight.FlightDetailResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FlightService {
    Page<FlightResponseDTO> list(FlightFilterDTO filter, Pageable pageable);
    Page<FlightResponseDTO> listForPassenger(UUID passengerId, FlightFilterDTO filter, Pageable pageable);
    FlightDetailResponseDTO getById(Long id, boolean includePassengers);
    FlightDetailResponseDTO getByIdForPassenger(Long id, UUID passengerId);
    FlightResponseDTO create(FlightCreateDTO dto);
    FlightResponseDTO update(Long id, FlightCreateDTO dto);
    void delete(Long id);
}
