package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "passenger", indexes = {
    @Index(name = "idx_passenger_doc", columnList = "docNumber")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String docType;

    @Column(nullable = false)
    private String docNumber;

    @Column(nullable = false)
    private String nationality;

    private String loyaltyTier;

    private String emergencyContact;

}
