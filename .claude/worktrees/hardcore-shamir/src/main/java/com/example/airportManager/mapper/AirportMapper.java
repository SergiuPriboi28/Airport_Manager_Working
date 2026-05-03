package com.example.airportManager.mapper;

import com.example.airportManager.dto.airport.AirportCreateDTO;
import com.example.airportManager.dto.airport.AirportResponseDTO;
import com.example.airportManager.dto.airport.AirportUpdateDTO;
import com.example.airportManager.model.Airport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AirportMapper {
    Airport toEntity(AirportCreateDTO dto);
    void update(@MappingTarget Airport entity, AirportUpdateDTO dto);
    AirportResponseDTO toResponse(Airport entity);
}
