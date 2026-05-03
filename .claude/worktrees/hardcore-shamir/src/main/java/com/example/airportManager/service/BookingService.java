package com.example.airportManager.service;

import com.example.airportManager.dto.booking.BookingCreateDTO;
import com.example.airportManager.dto.booking.BookingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDTO create(BookingCreateDTO dto, UUID userId);
    BookingResponseDTO getById(Long id);
    List<BookingResponseDTO> getMyBookings(UUID userId);
    BookingResponseDTO cancel(Long id, UUID userId);
}
