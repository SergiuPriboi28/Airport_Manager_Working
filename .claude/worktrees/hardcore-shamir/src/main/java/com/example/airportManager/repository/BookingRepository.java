package com.example.airportManager.repository;

import com.example.airportManager.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.passengerProfile.userId = :passengerId")
    List<Booking> findByPassengerProfileUserId(@Param("passengerId") UUID passengerId);

    @Query("SELECT b FROM Booking b WHERE b.flight.id = :flightId")
    List<Booking> findByFlightId(@Param("flightId") Long flightId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.flight.id = :flightId AND b.passengerProfile.userId = :passengerId")
    boolean existsByFlightIdAndPassengerProfileUserId(@Param("flightId") Long flightId, @Param("passengerId") UUID passengerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.flight.id = :flightId AND b.status = com.example.airportManager.model.BookingStatus.CONFIRMED")
    long countConfirmedByFlightId(@Param("flightId") Long flightId);
}
