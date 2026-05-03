package com.example.airportManager.mapper;

import com.example.airportManager.dto.passenger.PassengerCreateDTO;
import com.example.airportManager.dto.passenger.PassengerResponseDTO;
import com.example.airportManager.model.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loyaltyTier", ignore = true)
    @Mapping(target = "emergencyContact", ignore = true)
Passenger toEntity(PassengerCreateDTO dto);

    PassengerResponseDTO toResponse(Passenger entity);
}
