package com.example.airportManager.web.controller;

import com.example.airportManager.dto.aircraft.AircraftCreateDTO;
import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import com.example.airportManager.service.AircraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aircraft")
@RequiredArgsConstructor
@Validated
public class AircraftController {

    private final AircraftService aircraftService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public Page<AircraftResponseDTO> list(
            @ParameterObject @PageableDefault(sort = "model", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return aircraftService.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AircraftResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AircraftResponseDTO> create(@Valid @RequestBody AircraftCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aircraftService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<AircraftResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AircraftCreateDTO dto
    ) {
        return ResponseEntity.ok(aircraftService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        aircraftService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
