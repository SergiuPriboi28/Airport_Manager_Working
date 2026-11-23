package com.example.airportManager.repository;

import com.example.airportManager.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
    Optional<Flight> findByCode(String code);

    @Override
    @EntityGraph(attributePaths = {"route", "route.originAirport", "route.destAirport", "aircraft"})
    Page<Flight> findAll(Specification<Flight> spec, Pageable pageable);
}
