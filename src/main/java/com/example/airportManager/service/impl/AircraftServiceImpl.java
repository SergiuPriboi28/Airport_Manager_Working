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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info("Creating aircraft: tailNumber={}", dto.tailNumber());
        if (aircraftRepository.findByTailNumber(dto.tailNumber()).isPresent()) {
            log.warn("Aircraft creation rejected: tailNumber={} already exists", dto.tailNumber());
            throw new ConflictException("Aircraft with tail number " + dto.tailNumber() + " already exists");
        }
        Aircraft aircraft = aircraftMapper.toEntity(dto);
        Aircraft saved = aircraftRepository.save(aircraft);
        log.info("Aircraft created: aircraftId={}, tailNumber={}", saved.getId(), saved.getTailNumber());
        return aircraftMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AircraftResponseDTO update(Long id, AircraftCreateDTO dto) {
        log.info("Updating aircraft: aircraftId={}", id);
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + id));

        aircraftRepository.findByTailNumber(dto.tailNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    log.warn("Aircraft update rejected: aircraftId={}, tailNumber={} already exists", id, dto.tailNumber());
                    throw new ConflictException("Aircraft with tail number " + dto.tailNumber() + " already exists");
                });

        aircraft.setTailNumber(dto.tailNumber());
        aircraft.setModel(dto.model());
        aircraft.setCapacity(dto.capacity());
        aircraft.setSeatMapRef(dto.seatMapRef());
        aircraft.setStatus(dto.status());

        Aircraft updated = aircraftRepository.save(aircraft);
        log.info("Aircraft updated: aircraftId={}, tailNumber={}", updated.getId(), updated.getTailNumber());
        return aircraftMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting aircraft: aircraftId={}", id);
        if (!aircraftRepository.existsById(id)) {
            throw new EntityNotFoundException("Aircraft not found with id: " + id);
        }
        aircraftRepository.deleteById(id);
        log.info("Aircraft deleted: aircraftId={}", id);
    }
}
