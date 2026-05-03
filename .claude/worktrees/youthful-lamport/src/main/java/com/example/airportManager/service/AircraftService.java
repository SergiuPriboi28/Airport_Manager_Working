package com.example.airportManager.service;

import com.example.airportManager.dto.aircraft.AircraftCreateDTO;
import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AircraftService {
    Page<AircraftResponseDTO> list(Pageable pageable);
    AircraftResponseDTO getById(Long id);
    AircraftResponseDTO create(AircraftCreateDTO dto);
    AircraftResponseDTO update(Long id, AircraftCreateDTO dto);
    void delete(Long id);
}
