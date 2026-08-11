package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.aircraft.AircraftCreateDTO;
import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import com.example.airportManager.mapper.AircraftMapper;
import com.example.airportManager.model.Aircraft;
import com.example.airportManager.repository.AircraftRepository;
import com.example.airportManager.service.impl.AircraftServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AircraftServiceImplTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftMapper aircraftMapper;

    @InjectMocks
    private AircraftServiceImpl aircraftService;

    private Aircraft aircraft;
    private AircraftResponseDTO responseDTO;
    private AircraftCreateDTO createDTO;


    @BeforeEach
    void setUp() {
        aircraft = new Aircraft();
        aircraft.setId(1L);
        aircraft.setTailNumber("YR-ABC");
        aircraft.setModel("Airbus A320");
        aircraft.setCapacity(180);
        aircraft.setSeatMapRef("A320-STD");
        aircraft.setStatus("ACTIVE");

        responseDTO = new AircraftResponseDTO(1L, "YR-ABC", "Airbus A320", 180, "A320-STD", "ACTIVE");
        createDTO = new AircraftCreateDTO("YR-ABC", "Airbus A320", 180, "A320-STD", "ACTIVE");
    }


    @Test
    void list_returnsMappedPage_respectingPageSize() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Aircraft> aircraftPage = new PageImpl<>(List.of(aircraft), pageable, 2);

        when(aircraftRepository.findAll(pageable)).thenReturn(aircraftPage);
        when(aircraftMapper.toResponse(aircraft)).thenReturn(responseDTO);

        Page<AircraftResponseDTO> result = aircraftService.list(pageable);

        assertThat(result.getContent()).containsExactly(responseDTO);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(aircraftRepository).findAll(pageable);
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