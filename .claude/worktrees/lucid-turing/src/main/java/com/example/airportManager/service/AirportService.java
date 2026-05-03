package com.example.airportManager.service;

import com.example.airportManager.dto.airport.AirportCreateDTO;
import com.example.airportManager.dto.airport.AirportResponseDTO;
import com.example.airportManager.dto.airport.AirportUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AirportService {
    Page<AirportResponseDTO> list(Pageable pageable);
    AirportResponseDTO create(AirportCreateDTO dto);
    AirportResponseDTO update(Long id, AirportUpdateDTO dto);
    void delete(Long id);
}
