package com.example.airportManager.config;

import com.example.airportManager.model.*;
import com.example.airportManager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;
    private final FlightRepository flightRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }

        if (airportRepository.findByIata("JFK").isEmpty()) {
            Airport jfk = new Airport();
            jfk.setIata("JFK");
            jfk.setIcao("KJFK");
            jfk.setName("John F. Kennedy International Airport");
            jfk.setCity("New York");
            jfk.setCountry("US");
            jfk.setTimezone("America/New_York");
            airportRepository.save(jfk);

            Airport lhr = new Airport();
            lhr.setIata("LHR");
            lhr.setIcao("EGLL");
            lhr.setName("Heathrow Airport");
            lhr.setCity("London");
            lhr.setCountry("GB");
            lhr.setTimezone("Europe/London");
            airportRepository.save(lhr);

            Route route = new Route();
            route.setOriginAirport(jfk);
            route.setDestAirport(lhr);
            route.setDistanceNm(3459);
            route.setStdDurationMin(420);
            routeRepository.save(route);

            Flight flight = new Flight();
            flight.setCode("AM001");
            flight.setRoute(route);
            flight.setDepartureScheduled(LocalDateTime.of(2026, 4, 13, 9, 0));
            flight.setArrivalScheduled(LocalDateTime.of(2026, 4, 13, 21, 0));
            flight.setGate("B12");
            flight.setStatus(FlightStatus.SCHEDULED);
            flightRepository.save(flight);
        }
    }
}
