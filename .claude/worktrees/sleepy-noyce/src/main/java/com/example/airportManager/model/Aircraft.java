package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "aircraft", indexes = {
    @Index(name = "idx_aircraft_tail", columnList = "tailNumber")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"flights"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String tailNumber;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int capacity;

    private String seatMapRef;

    private String status;

    @OneToMany(mappedBy = "aircraft", fetch = FetchType.LAZY)
    private Set<Flight> flights;

}
