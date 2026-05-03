package com.example.airportManager.service.impl;

import com.example.airportManager.dto.aircraft.AircraftCreateDTO;
import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.AircraftMapper;
import com.example.airportManager.model.Aircraft;
import com.example.airportManager.repository.AircraftRepository;
import com.example.airportManager.service.AircraftService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AircraftServiceImpl implements AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponseDTO> list(Pageable pageable) {
        return aircraftRepository.findAll(pageable).map(aircraftMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AircraftResponseDTO getById(Long id) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + id));
        return aircraftMapper.toResponse(aircraft);
    }

    @Override
    @Transactional
    public AircraftResponseDTO create(AircraftCreateDTO dto) {
        if (aircraftRepository.findByTailNumber(dto.tailNumber()).isPresent()) {
            throw new ConflictException("Aircraft with tail number " + dto.tailNumber() + " already exists");
        }
        Aircraft aircraft = aircraftMapper.toEntity(dto);
        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Override
    @Transactional
    public AircraftResponseDTO update(Long id, AircraftCreateDTO dto) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + id));

        aircraftRepository.findByTailNumber(dto.tailNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Aircraft with tail number " + dto.tailNumber() + " already exists");
                });

        aircraft.setTailNumber(dto.tailNumber());
        aircraft.setModel(dto.model());
        aircraft.setCapacity(dto.capacity());
        aircraft.setSeatMapRef(dto.seatMapRef());
        aircraft.setStatus(dto.status());

        return aircraftMapper.toResponse(aircraftRepository.save(aircraft));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!aircraftRepository.existsById(id)) {
            throw new EntityNotFoundException("Aircraft not found with id: " + id);
        }
        aircraftRepository.deleteById(id);
    }
}
