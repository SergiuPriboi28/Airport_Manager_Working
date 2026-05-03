package com.example.airportManager.mapper;

import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.model.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "gate", ignore = true)
    @Mapping(target = "departureScheduled", source = "departureTime")
    @Mapping(target = "arrivalScheduled", source = "arrivalTime")
    Flight toEntity(FlightCreateDTO dto);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "departureTime", source = "departureScheduled")
    @Mapping(target = "arrivalTime", source = "arrivalScheduled")
    FlightResponseDTO toResponse(Flight entity);
}
