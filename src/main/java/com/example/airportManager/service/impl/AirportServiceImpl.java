package com.example.airportManager.service.impl;

import com.example.airportManager.dto.airport.AirportCreateDTO;
import com.example.airportManager.dto.airport.AirportResponseDTO;
import com.example.airportManager.dto.airport.AirportUpdateDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.AirportMapper;
import com.example.airportManager.model.Airport;
import com.example.airportManager.repository.AirportRepository;
import com.example.airportManager.service.AirportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AirportServiceImpl implements AirportService {
    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;

    @Autowired
    public AirportServiceImpl(AirportRepository airportRepository, AirportMapper airportMapper){
        this.airportRepository = airportRepository;
        this.airportMapper = airportMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirportResponseDTO> list(Pageable pageable) {
        return airportRepository.findAll(pageable)
                .map(airportMapper::toResponse);
    }

    @Override
    @Transactional
    public AirportResponseDTO create(AirportCreateDTO dto) {
        if (airportRepository.findByIata(dto.iata()).isPresent()) {
            throw new ConflictException("Airport with IATA " + dto.iata() + " already exists");
        }
        Airport airport = airportMapper.toEntity(dto);
        Airport saved = airportRepository.save(airport);
        return airportMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AirportResponseDTO update(Long id, AirportUpdateDTO dto) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Airport not found with id: " + id));
        airportMapper.update(airport, dto);
        Airport updated = airportRepository.save(airport);
        return airportMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new EntityNotFoundException("Airport not found with id: " + id);
        }
        airportRepository.deleteById(id);
    }
}
