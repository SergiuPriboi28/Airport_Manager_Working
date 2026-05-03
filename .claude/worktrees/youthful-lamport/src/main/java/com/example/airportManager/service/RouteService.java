package com.example.airportManager.service;

import com.example.airportManager.dto.route.RouteCreateDTO;
import com.example.airportManager.dto.route.RouteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RouteService {
    Page<RouteResponseDTO> list(Pageable pageable, Optional<Long> originId, Optional<Long> destId);
    RouteResponseDTO create(RouteCreateDTO dto);
    void delete(Long id);
}
