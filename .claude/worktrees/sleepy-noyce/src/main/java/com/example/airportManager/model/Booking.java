package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking", indexes = {
    @Index(name = "idx_booking_pnr", columnList = "pnr")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_profile_id", nullable = false)
    private PassengerProfile passengerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(unique = true, nullable = false)
    private String pnr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
}
