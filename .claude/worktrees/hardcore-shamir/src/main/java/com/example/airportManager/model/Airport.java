package com.example.airportManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "airport", indexes = {
    @Index(name = "idx_airport_iata", columnList = "iata"),
    @Index(name = "idx_airport_icao", columnList = "icao")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"originRoutes", "destRoutes"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 3)
    private String iata;

    @Column(unique = true, nullable = false, length = 4)
    private String icao;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String timezone;

    @OneToMany(mappedBy = "originAirport", fetch = FetchType.LAZY)
    private Set<Route> originRoutes;

    @OneToMany(mappedBy = "destAirport", fetch = FetchType.LAZY)
    private Set<Route> destRoutes;

}
