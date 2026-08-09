package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import com.example.airportManager.mapper.AircraftMapper;
import com.example.airportManager.model.Aircraft;
import com.example.airportManager.repository.AircraftRepository;
import com.example.airportManager.service.AircraftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AircraftServiceImpl {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftMapper aircraftMapper;

    @InjectMocks
    private AircraftServiceImpl aircraftServiceImpl;

    private Aircraft aircraft;
    private AircraftResponseDTO aircraftResponseDTO;

    @Test
    void list_returnsMappedPage_respectingPageSize(){
        aircraft = new  Aircraft();
        aircraft.setId(1L);
        aircraft.setTailNumber("YR-ABC");
        aircraft.setModel("Airbus A320");
        aircraft.setCapacity(180);
        aircraft.setSeatMapRef("Map1");
        aircraft.setStatus("Active");

        aircraftResponseDTO = new AircraftResponseDTO(1L, "YR-ABC", "Airbus A320", 180, "A320-STD", "ACTIVE");


    }




}


//AircraftServiceImpl (5 methods)
//
//list(Pageable) — returns a mapped page; respects page size
//
//getById(Long) — found; throws NotFoundException when absent
//
//create(AircraftCreateDTO) — persists and returns the mapped response
//
//update(Long, AircraftCreateDTO) — mutates the loaded entity; not-found path
//
//delete(Long) — deletes; not-found path; behaviour when the aircraft is referenced by a flight