package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.aircraft.AircraftCreateDTO;
import com.example.airportManager.dto.aircraft.AircraftResponseDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.AircraftMapper;
import com.example.airportManager.model.Aircraft;
import com.example.airportManager.repository.AircraftRepository;
import com.example.airportManager.service.AircraftService;
import com.example.airportManager.service.impl.AircraftServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    private AircraftResponseDTO updatedDTO;
    private AircraftCreateDTO createDTO;
    private AircraftCreateDTO updateDTO;


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
        updateDTO = new AircraftCreateDTO("YR-DEF", "Airbus A320", 180, "A320-STD", "ACTIVE");
        updatedDTO = new AircraftResponseDTO(1L, "YR-DEF", "Airbus A320", 180, "A320-STD", "ACTIVE");
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

    @Test
    void getById_returnsFound(){
        when(aircraftRepository.findById(1L)).thenReturn(Optional.ofNullable(aircraft));
        when(aircraftMapper.toResponse(aircraft)).thenReturn(responseDTO);

        AircraftResponseDTO result = aircraftService.getById(1L);

        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    void getById_returnsError_whenAbsent() {
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aircraftService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Aircraft not found with id: 99");
    }

    @Test
    void create_PersistsAndReturns_MappedResponse(){
        when(aircraftRepository.findByTailNumber("YR-ABC")).thenReturn(Optional.empty());
        when(aircraftMapper.toEntity(createDTO)).thenReturn(aircraft);
        when(aircraftRepository.save(aircraft)).thenReturn(aircraft);
        when(aircraftMapper.toResponse(aircraft)).thenReturn(responseDTO);

        AircraftResponseDTO result = aircraftService.create(createDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(aircraftRepository).save(aircraft);
    }

    @Test
    void create_fromExistingsReturnsExc(){
        when(aircraftRepository.findByTailNumber("YR-ABC")).thenReturn(Optional.of(aircraft));

        assertThatThrownBy(()->aircraftService.create(createDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Aircraft with tail number YR-ABC already exists");

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void update_mutatesGivenEntity(){
        when(aircraftRepository.findById(1L)).thenReturn(Optional.ofNullable(aircraft));
        when(aircraftRepository.findByTailNumber("YR-DEF")).thenReturn(Optional.empty());
        when(aircraftMapper.toResponse(aircraft)).thenReturn(responseDTO);
        when(aircraftRepository.save(aircraft)).thenReturn(aircraft);

        aircraftService.update(1L,updateDTO);

        assertThat(aircraft.getTailNumber()).isEqualTo("YR-DEF");
        verify(aircraftRepository).save(aircraft);
    }

    @Test
    void update_notFoundThrowsException(){
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->aircraftService.update(99L,updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Aircraft not found with id: 99");

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void delete_works(){
        when(aircraftRepository.existsById(1L)).thenReturn(true);

        aircraftService.delete(1L);

        verify(aircraftRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundExceptionAndDoesNotDelete() {
        when(aircraftRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> aircraftService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(aircraftRepository, never()).deleteById(any());
    }

    @Test
    void delete_referencedByFlight_propagatesDataIntegrityViolationException() {
        when(aircraftRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Referenced by flight"))
                .when(aircraftRepository).deleteById(1L);

        assertThatThrownBy(() -> aircraftService.delete(1L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}