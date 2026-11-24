package com.example.airportManager.service.impl;

import com.example.airportManager.dto.booking.BookingCreateDTO;
import com.example.airportManager.dto.booking.BookingResponseDTO;
import com.example.airportManager.mapper.BookingMapper;
import com.example.airportManager.model.Booking;
import com.example.airportManager.model.BookingStatus;
import com.example.airportManager.model.Flight;
import com.example.airportManager.model.PassengerProfile;
import com.example.airportManager.repository.BookingRepository;
import com.example.airportManager.repository.FlightRepository;
import com.example.airportManager.repository.PassengerProfileRepository;
import com.example.airportManager.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final FlightRepository flightRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDTO create(BookingCreateDTO dto, UUID userId) {
        log.info("Creating booking: userId={}, flightId={}", userId, dto.flightId());
        PassengerProfile passengerProfile = passengerProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger profile not found for user: " + userId));
        Flight flight = flightRepository.findById(dto.flightId())
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + dto.flightId()));

        int capacity = flight.getAircraft() != null ? flight.getAircraft().getCapacity() : 0;
        long confirmed = bookingRepository.countConfirmedByFlightId(flight.getId());
        if (confirmed >= capacity) {
            log.warn("Booking rejected: flightId={} is fully booked, userId={}", flight.getId(), userId);
            throw new IllegalStateException("Flight " + flight.getCode() + " is fully booked");
        }

        Booking booking = new Booking();
        booking.setPassengerProfile(passengerProfile);
        booking.setFlight(flight);
        booking.setPnr(generatePNR());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: bookingId={}, pnr={}, flightId={}, userId={}",
                saved.getId(), saved.getPnr(), flight.getId(), userId);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getMyBookings(UUID userId) {
        return bookingRepository.findByPassengerProfileUserId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDTO cancel(Long id, UUID userId) {
        log.info("Cancelling booking: bookingId={}, userId={}", id, userId);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        if (!booking.getPassengerProfile().getUserId().equals(userId)) {
            log.warn("Booking cancel denied: bookingId={}, userId={} does not own this booking", id, userId);
            throw new AccessDeniedException("You do not own this booking");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        BookingResponseDTO response = bookingMapper.toResponse(bookingRepository.save(booking));
        log.info("Booking cancelled: bookingId={}, userId={}", id, userId);
        return response;
    }

    private String generatePNR() {
        return "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
