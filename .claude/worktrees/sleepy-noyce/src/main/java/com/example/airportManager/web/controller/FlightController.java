package com.example.airportManager.web.controller;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightDetailResponseDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.model.FlightStatus;
import com.example.airportManager.security.CustomUserDetails;
import com.example.airportManager.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Validated
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'EMPLOYEE', 'ADMIN')")
    public Page<FlightResponseDTO> list(
            @ParameterObject @PageableDefault(sort = "departureScheduled", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dateTo,
            @RequestParam Optional<Long> routeId,
            @RequestParam Optional<FlightStatus> status,
            @RequestParam Optional<String> origin,
            @RequestParam Optional<String> destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> date
    ) {
        return flightService.list(pageable, dateFrom, dateTo, routeId, status, origin, destination, date);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PASSENGER')")
    public Page<FlightResponseDTO> listMyFlights(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject @PageableDefault(sort = "departureScheduled", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dateTo,
            @RequestParam Optional<Long> routeId,
            @RequestParam Optional<FlightStatus> status,
            @RequestParam Optional<String> origin,
            @RequestParam Optional<String> destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> date
    ) {
        return flightService.listForPassenger(userDetails.getUserId(), pageable, dateFrom, dateTo, routeId, status, origin, destination, date);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<FlightDetailResponseDTO> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        if (userDetails.getUser().hasRole(RoleName.EMPLOYEE) || userDetails.getUser().hasRole(RoleName.ADMIN)) {
            return ResponseEntity.ok(flightService.getById(id, true));
        } else {
            return ResponseEntity.ok(flightService.getByIdForPassenger(id, userDetails.getUserId()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<FlightResponseDTO> create(@Valid @RequestBody FlightCreateDTO dto) {
        FlightResponseDTO created = flightService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<FlightResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FlightCreateDTO dto
    ) {
        FlightResponseDTO updated = flightService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
