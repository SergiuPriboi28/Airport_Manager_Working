package com.example.airportManager.repository;

import com.example.airportManager.model.PassengerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerProfileRepository extends JpaRepository<PassengerProfile, UUID> {
}
