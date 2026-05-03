package com.example.airportManager.service.impl;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightDetailResponseDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.dto.flight.PassengerInfoDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.FlightMapper;
import com.example.airportManager.model.Booking;
import com.example.airportManager.model.Flight;
import com.example.airportManager.model.FlightStatus;
import com.example.airportManager.model.Route;
import com.example.airportManager.repository.BookingRepository;
import com.example.airportManager.repository.FlightRepository;
import com.example.airportManager.repository.RouteRepository;
import com.example.airportManager.service.FlightService;
import com.example.airportManager.spec.FlightSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final BookingRepository bookingRepository;
    private final FlightMapper flightMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponseDTO> list(
            Pageable pageable,
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo,
            Optional<Long> routeId,
            Optional<FlightStatus> status
    ) {
        Specification<Flight> spec = Specification.where(null);
        if (dateFrom.isPresent() || dateTo.isPresent()) {
            spec = spec.and(FlightSpecifications.departureBetween(dateFrom.orElse(null), dateTo.orElse(null)));
        }
        if (routeId.isPresent()) {
            spec = spec.and(FlightSpecifications.hasRouteId(routeId.get()));
        }
        if (status.isPresent()) {
            spec = spec.and(FlightSpecifications.hasStatus(status.get()));
        }
        return flightRepository.findAll(spec, pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    @Transactional
    public FlightResponseDTO create(FlightCreateDTO dto) {
        if (flightRepository.findByCode(dto.code()).isPresent()) {
            throw new ConflictException("Flight with code " + dto.code() + " already exists");
        }

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + dto.routeId()));

        Flight flight = flightMapper.toEntity(dto);
        flight.setRoute(route);

        Flight saved = flightRepository.save(flight);
        return flightMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FlightResponseDTO update(Long id, FlightCreateDTO dto) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + id));

        Optional<Flight> existingWithCode = flightRepository.findByCode(dto.code());
        if (existingWithCode.isPresent() && !existingWithCode.get().getId().equals(id)) {
            throw new ConflictException("Flight with code " + dto.code() + " already exists");
        }

        Route route = routeRepository.findById(dto.routeId())
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + dto.routeId()));

        flight.setCode(dto.code());
        flight.setRoute(route);
        flight.setDepartureScheduled(dto.departureTime());
        flight.setArrivalScheduled(dto.arrivalTime());
        flight.setStatus(dto.status());

        Flight updated = flightRepository.save(flight);
        return flightMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponseDTO> listForPassenger(
            UUID passengerId,
            Pageable pageable,
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo,
            Optional<Long> routeId,
            Optional<FlightStatus> status
    ) {
        List<Booking> bookings = bookingRepository.findByPassengerProfileUserId(passengerId);
        List<Long> flightIds = bookings.stream()
                .map(b -> b.getFlight().getId())
                .collect(Collectors.toList());

        if (flightIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Specification<Flight> spec = Specification.where((root, query, cb) -> root.get("id").in(flightIds));
        if (dateFrom.isPresent() || dateTo.isPresent()) {
            spec = spec.and(FlightSpecifications.departureBetween(dateFrom.orElse(null), dateTo.orElse(null)));
        }
        if (routeId.isPresent()) {
            spec = spec.and(FlightSpecifications.hasRouteId(routeId.get()));
        }
        if (status.isPresent()) {
            spec = spec.and(FlightSpecifications.hasStatus(status.get()));
        }
        return flightRepository.findAll(spec, pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightDetailResponseDTO getById(Long id, boolean includePassengers) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + id));

        List<PassengerInfoDTO> passengers = Collections.emptyList();
        if (includePassengers) {
            List<Booking> bookings = bookingRepository.findByFlightId(id);
            passengers = bookings.stream()
                    .map(b -> new PassengerInfoDTO(
                            b.getPassengerProfile().getUserId(),
                            b.getPassengerProfile().getUser().getEmail(),
                            b.getPassengerProfile().getDocType(),
                            b.getPassengerProfile().getDocNumber(),
                            b.getPassengerProfile().getNationality(),
                            b.getPnr(),
                            b.getStatus().name()
                    ))
                    .collect(Collectors.toList());
        }

        return new FlightDetailResponseDTO(
                flight.getId(),
                flight.getCode(),
                flight.getRoute().getId(),
                flight.getDepartureScheduled(),
                flight.getArrivalScheduled(),
                flight.getStatus(),
                flight.getGate(),
                passengers
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FlightDetailResponseDTO getByIdForPassenger(Long id, UUID passengerId) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + id));

        if (!bookingRepository.existsByFlightIdAndPassengerProfileUserId(id, passengerId)) {
            throw new AccessDeniedException("You are not registered for this flight");
        }

        return new FlightDetailResponseDTO(
                flight.getId(),
                flight.getCode(),
                flight.getRoute().getId(),
                flight.getDepartureScheduled(),
                flight.getArrivalScheduled(),
                flight.getStatus(),
                flight.getGate(),
                Collections.emptyList()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new EntityNotFoundException("Flight not found with id: " + id);
        }
        flightRepository.deleteById(id);
    }
}
