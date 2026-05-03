package com.example.airportManager.spec;

import com.example.airportManager.model.Flight;
import com.example.airportManager.model.FlightStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FlightSpecifications {
    private FlightSpecifications() {}

    public static Specification<Flight> departureBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) {
                return cb.between(root.get("departureScheduled"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("departureScheduled"), from);
            } else {
                return cb.lessThanOrEqualTo(root.get("departureScheduled"), to);
            }
        };
    }

    public static Specification<Flight> hasRouteId(Long routeId) {
        return (root, query, cb) -> routeId == null ? null : cb.equal(root.get("route").get("id"), routeId);
    }

    public static Specification<Flight> hasStatus(FlightStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Flight> hasOriginCity(String city) {
        return (root, query, cb) -> city == null ? null :
            cb.like(cb.lower(root.get("route").get("originAirport").get("city")),
                    "%" + city.toLowerCase() + "%");
    }

    public static Specification<Flight> hasDestCity(String city) {
        return (root, query, cb) -> city == null ? null :
            cb.like(cb.lower(root.get("route").get("destAirport").get("city")),
                    "%" + city.toLowerCase() + "%");
    }

    public static Specification<Flight> departureOnDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
            cb.between(root.get("departureScheduled"),
                       date.atStartOfDay(),
                       date.plusDays(1).atStartOfDay());
    }
}
