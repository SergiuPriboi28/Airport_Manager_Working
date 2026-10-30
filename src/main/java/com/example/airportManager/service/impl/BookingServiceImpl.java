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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        PassengerProfile passengerProfile = passengerProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger profile not found for user: " + userId));
        Flight flight = flightRepository.findById(dto.flightId())
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + dto.flightId()));

        if (flight.getAircraft() == null){
            throw new IllegalStateException("No aircraft is assigned to this flight");
        }

        int capacity = flight.getAircraft() != null ? flight.getAircraft().getCapacity() : 0;
        long confirmed = bookingRepository.countConfirmedByFlightId(flight.getId());
        if (confirmed >= capacity) {
            throw new IllegalStateException("Flight " + flight.getCode() + " is fully booked");
        }

        Booking booking = new Booking();
        booking.setPassengerProfile(passengerProfile);
        booking.setFlight(flight);
        booking.setPnr(generatePNR());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
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
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
        if (!booking.getPassengerProfile().getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this booking");
        }
        if (booking.getStatus().equals(BookingStatus.CANCELLED)){
            throw new IllegalStateException("Booking already cancelled!");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    public String generatePNR() {
        return "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
