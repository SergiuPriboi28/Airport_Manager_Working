package com.example.airportManager.service.impl;

import com.example.airportManager.dto.airport.AirportUpdateDTO;
import com.example.airportManager.dto.airport.AirportResponseDTO;
import com.example.airportManager.mapper.AirportMapper;
import com.example.airportManager.model.Airport;
import com.example.airportManager.repository.AirportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// Unit test for AirportServiceImpl. This is a pure Mockito unit test rather than a Spring
// integration test: @ExtendWith(MockitoExtension.class) boots Mockito's JUnit5 integration
// WITHOUT starting a Spring application context, so @Mock/@InjectMocks fields are initialized
// and the whole test class runs in milliseconds. Each @Mock field is a fake collaborator
// (repository/mapper) that returns exactly what the test stubs with when(...) - no real
// database is ever hit. @InjectMocks instead builds a REAL AirportServiceImpl instance and
// wires the mocks into it via the constructor (here an explicit @Autowired constructor rather
// than Lombok, but the effect is the same), so the service's own logic - lookups, not-found
// handling, delegating to the mapper - runs unmodified while its dependencies are faked. This
// isolates AirportServiceImpl's own behavior from the database and the mapper implementation,
// so a failing test points directly at a bug in this service.
@ExtendWith(MockitoExtension.class)
class AirportServiceImplTestExample {

    // Fake repository collaborator: we control exactly what findById/save/existsById return,
    // no real DB is touched.
    @Mock
    private AirportRepository airportRepository;

    // Fake mapper collaborator: converts between DTOs and the Airport entity; stubbed so the
    // test doesn't depend on the real mapping implementation.
    @Mock
    private AirportMapper airportMapper;

    // The real, unmocked AirportServiceImpl under test, with the two mocks above injected.
    @InjectMocks
    private AirportServiceImpl airportService;

    // Happy-path update: verifies that when the airport exists, update() looks it up, delegates
    // the field-by-field copy to the mapper (a void, @MappingTarget-style call), saves the
    // mutated entity, and returns the mapped response.
    @Test
    void update_shouldSaveUpdatedAirport_whenAirportExists() {
        // Arrange
        Long id = 1L;
        Airport existing = new Airport();
        existing.setId(id);
        existing.setIata("OTP");

        AirportUpdateDTO dto = new AirportUpdateDTO("OTP", "LROP", "Henri Coanda", "Bucuresti", "Romania", "EET");
        AirportResponseDTO expectedResponse =
                new AirportResponseDTO(id, "OTP", "LROP", "Henri Coanda", "Bucuresti", "Romania", "EET");

        when(airportRepository.findById(id)).thenReturn(Optional.of(existing));
        // airportMapper.update(...) e o metoda void care modifica "existing" pe loc (MapStruct @MappingTarget).
        // Nu trebuie sa facem when(...) pentru metode void - Mockito nu face nimic implicit, ceea ce e ok aici
        // pentru ca noi verificam doar ca save() a fost apelat cu entitatea corecta mai jos.
        when(airportRepository.save(existing)).thenReturn(existing);
        when(airportMapper.toResponse(existing)).thenReturn(expectedResponse);

        // Act
        AirportResponseDTO result = airportService.update(id, dto);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(airportMapper).update(existing, dto);
        verify(airportRepository).save(existing);
    }

    // Not-found edge case: verifies update() fails fast with a descriptive
    // EntityNotFoundException (carrying the missing id) instead of attempting to mutate/save a
    // non-existent entity.
    @Test
    void update_shouldThrowEntityNotFoundException_whenAirportDoesNotExist() {
        // Arrange
        Long id = 42L;
        AirportUpdateDTO dto = new AirportUpdateDTO("OTP", "LROP", "Henri Coanda", "Bucuresti", "Romania", "EET");
        when(airportRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        // assertThatThrownBy(...) is AssertJ's fluent exception-assertion API: it runs the
        // lambda, captures the thrown exception, and lets us chain type/message assertions on it.
        assertThatThrownBy(() -> airportService.update(id, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("42");

        // Daca aeroportul nu exista, nu ar trebui sa se incerce niciun save.
        // any() is a Mockito argument matcher ("any Airport instance") - we only care that
        // save() was never called at all, regardless of what would have been passed to it.
        verify(airportRepository, never()).save(any());
    }

    // Deletion happy path: verifies delete() forwards to the repository's deleteById() once the
    // existence check passes. Since delete() returns void, the assertion is behavioral
    // (verify(...) that the call happened) rather than a return-value check.
    @Test
    void delete_shouldRemoveAirport_whenItExists() {
        // Arrange
        Long id = 1L;
        when(airportRepository.existsById(id)).thenReturn(true);

        // Act
        airportService.delete(id);

        // Assert: pentru metode void, verificam ca actiunea a avut loc, nu o valoare de return.
        verify(airportRepository).deleteById(id);
    }
}