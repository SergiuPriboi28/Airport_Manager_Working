package com.example.airportManager.spec;

import com.example.airportManager.model.Route;
import org.springframework.data.jpa.domain.Specification;

public final class RouteSpecifications {
    private RouteSpecifications() {}

    public static Specification<Route> hasOriginAirportId(Long originId) {
        return (root, query, cb) -> originId == null ? null : cb.equal(root.get("originAirport").get("id"), originId);
    }

    public static Specification<Route> hasDestAirportId(Long destId) {
        return (root, query, cb) -> destId == null ? null : cb.equal(root.get("destAirport").get("id"), destId);
    }
}
