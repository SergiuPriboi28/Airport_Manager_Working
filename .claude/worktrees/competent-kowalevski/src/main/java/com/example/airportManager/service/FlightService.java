package com.example.airportManager.service;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.dto.flight.FlightDetailResponseDTO;
import com.example.airportManager.model.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface FlightService {
    Page<FlightResponseDTO> list(
            Pageable pageable,
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo,
            Optional<Long> routeId,
            Optional<FlightStatus> status
    );
    Page<FlightResponseDTO> listForPassenger(
            UUID passengerId,
            Pageable pageable,
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo,
            Optional<Long> routeId,
            Optional<FlightStatus> status
    );
    FlightDetailResponseDTO getById(Long id, boolean includePassengers);
    FlightDetailResponseDTO getByIdForPassenger(Long id, UUID passengerId);
    FlightResponseDTO create(FlightCreateDTO dto);
    FlightResponseDTO update(Long id, FlightCreateDTO dto);
    void delete(Long id);
}
