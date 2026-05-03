package com.example.airportManager.service.impl;

import com.example.airportManager.dto.route.RouteCreateDTO;
import com.example.airportManager.dto.route.RouteResponseDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.RouteMapper;
import com.example.airportManager.model.Airport;
import com.example.airportManager.model.Route;
import com.example.airportManager.repository.AirportRepository;
import com.example.airportManager.repository.RouteRepository;
import com.example.airportManager.service.RouteService;
import com.example.airportManager.spec.RouteSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final RouteMapper routeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<RouteResponseDTO> list(Pageable pageable, Optional<Long> originId, Optional<Long> destId) {
        Specification<Route> spec = Specification.where(null);
        if (originId.isPresent()) {
            spec = spec.and(RouteSpecifications.hasOriginAirportId(originId.get()));
        }
        if (destId.isPresent()) {
            spec = spec.and(RouteSpecifications.hasDestAirportId(destId.get()));
        }
        return routeRepository.findAll(spec, pageable)
                .map(routeMapper::toResponse);
    }

    @Override
    @Transactional
    public RouteResponseDTO create(RouteCreateDTO dto) {
        if (routeRepository.existsByOriginAirportIdAndDestAirportId(dto.originAirportId(), dto.destAirportId())) {
            throw new ConflictException("Route already exists for this origin-destination pair");
        }

        Airport originAirport = airportRepository.findById(dto.originAirportId())
                .orElseThrow(() -> new EntityNotFoundException("Origin airport not found with id: " + dto.originAirportId()));
        Airport destAirport = airportRepository.findById(dto.destAirportId())
                .orElseThrow(() -> new EntityNotFoundException("Destination airport not found with id: " + dto.destAirportId()));

        Route route = routeMapper.toEntity(dto);
        route.setOriginAirport(originAirport);
        route.setDestAirport(destAirport);

        Route saved = routeRepository.save(route);
        return routeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new EntityNotFoundException("Route not found with id: " + id);
        }
        routeRepository.deleteById(id);
    }
}
