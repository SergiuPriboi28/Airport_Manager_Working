package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "passenger_profile", indexes = {
    @Index(name = "idx_passenger_profile_doc", columnList = "docNumber")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "bookings"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PassengerProfile {
    @Id
    @EqualsAndHashCode.Include
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String docType;

    @Column(nullable = false)
    private String docNumber;

    @Column(nullable = false)
    private String nationality;

    private String loyaltyTier;

    private String emergencyContact;

    @OneToMany(mappedBy = "passengerProfile", fetch = FetchType.LAZY)
    private Set<Booking> bookings;
}
