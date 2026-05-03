package com.example.airportManager.repository;

import com.example.airportManager.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    boolean existsByOriginAirportIdAndDestAirportId(Long originId, Long destId);
}
