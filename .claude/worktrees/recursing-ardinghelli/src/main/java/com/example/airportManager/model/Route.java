package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "route", uniqueConstraints = {
    @UniqueConstraint(name = "uk_route_airports", columnNames = {"origin_airport_id", "dest_airport_id"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"flights"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private int distanceNm;

    @Column(nullable = false)
    private int stdDurationMin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private Airport originAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_airport_id", nullable = false)
    private Airport destAirport;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private Set<Flight> flights;
}
