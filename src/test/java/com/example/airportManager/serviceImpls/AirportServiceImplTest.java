package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.airport.AirportCreateDTO;
import com.example.airportManager.dto.airport.AirportResponseDTO;
import com.example.airportManager.dto.airport.AirportUpdateDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.AirportMapper;
import com.example.airportManager.model.Airport;
import com.example.airportManager.repository.AirportRepository;
import com.example.airportManager.service.impl.AirportServiceImpl;
import jakarta.inject.Inject;
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
public class AirportServiceImplTest {
    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AirportMapper airportMapper;

    @InjectMocks
    private AirportServiceImpl airportService;

    private Airport airport;
    private AirportResponseDTO airportResponseDTO;
    private AirportCreateDTO airportCreateDTO;
    private AirportUpdateDTO airportUpdateDTO;

    @BeforeEach
    void setUp() {
        airport = new Airport();
        airport.setId(1L);
        airport.setIata("OTP");
        airport.setIcao("LROP");
        airport.setName("Henri Coanda International");
        airport.setCity("Bucharest");
        airport.setCountry("Romania");
        airport.setTimezone("Europe/Bucharest");

        airportResponseDTO = new AirportResponseDTO(
                1L, "OTP", "LROP", "Henri Coanda International", "Bucharest", "Romania", "Europe/Bucharest");
        airportCreateDTO = new AirportCreateDTO(
                "OTP", "LROP", "Henri Coanda International", "Bucharest", "Romania", "Europe/Bucharest");
        airportUpdateDTO = new AirportUpdateDTO(
                "CLJ", "LRCL", "Cluj-Napoca International", "Cluj-Napoca", "Romania", "Europe/Bucharest");
    }

    @Test
    void list_works(){
        Pageable pageable = PageRequest.of(0,1);
        Page<Airport> airportPage = new PageImpl<>(List.of(airport),pageable,2);

        when(airportRepository.findAll(pageable)).thenReturn(airportPage);
        when(airportMapper.toResponse(airport)).thenReturn(airportResponseDTO);

        Page<AirportResponseDTO> result = airportService.list(pageable);

        assertThat(result.getContent()).containsExactly(airportResponseDTO);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(airportRepository).findAll(pageable);
    }


    @Test
    void create_works(){
        when(airportMapper.toEntity(airportCreateDTO)).thenReturn(airport);
        when(airportRepository.save(airport)).thenReturn(airport);
        when(airportMapper.toResponse(airport)).thenReturn(airportResponseDTO);

        AirportResponseDTO result = airportService.create(airportCreateDTO);

        assertThat(result).isEqualTo(airportResponseDTO);
        verify(airportRepository).save(airport);
    }

    @Test
    void create_duplicateIATAICAO(){
        when(airportRepository.findByIata("OTP")).thenReturn(Optional.of(airport));

        assertThatThrownBy(()-> airportService.create(airportCreateDTO))
                .isInstanceOf(ConflictException.class);

        verify(airportRepository, never()).save(any());
    }

    @Test
    void update_works(){
        when(airportRepository.findById(1L)).thenReturn(Optional.ofNullable(airport));
        when(airportRepository.save(airport)).thenReturn(airport);
        when(airportMapper.toResponse(airport)).thenReturn(airportResponseDTO);

        AirportResponseDTO result = airportService.update(1L, airportUpdateDTO);

        assertThat(result).isEqualTo(airportResponseDTO);
        verify(airportMapper).update(airport, airportUpdateDTO);
        verify(airportRepository).save(airport);
    }

    @Test
    void update_notFound_throwsEntityNotFoundExceptionAndDoesNotSave() {
        when(airportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> airportService.update(99L, airportUpdateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(airportRepository, never()).save(any());
    }

    @Test
    void delete_removesAirport() {
        when(airportRepository.existsById(1L)).thenReturn(true);

        airportService.delete(1L);

        verify(airportRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundExceptionAndDoesNotDelete() {
        when(airportRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> airportService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(airportRepository, never()).deleteById(any());
    }

    @Test
    void delete_referencedByRoute_propagatesDataIntegrityViolationException() {
        when(airportRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Referenced by route"))
                .when(airportRepository).deleteById(1L);

        assertThatThrownBy(() -> airportService.delete(1L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

//    AirportServiceImpl (4 methods)
//    update(Long, AirportUpdateDTO)
//    delete(Long) — what happens when routes still reference this airport

}
