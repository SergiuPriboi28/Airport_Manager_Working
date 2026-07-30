package com.example.airportManager.serviceImpl;

import com.example.airportManager.dto.auth.RegisterRequestDTO;
import com.example.airportManager.dto.flight.FlightCreateDTO;
import com.example.airportManager.dto.flight.FlightResponseDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.FlightMapper;
import com.example.airportManager.model.Flight;
import com.example.airportManager.model.FlightStatus;
import com.example.airportManager.model.Route;
import com.example.airportManager.repository.AircraftRepository;
import com.example.airportManager.repository.BookingRepository;
import com.example.airportManager.repository.FlightRepository;
import com.example.airportManager.repository.RouteRepository;
import com.example.airportManager.service.impl.FlightServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class flightServiceImplTest {
    @Mock
    private FlightRepository flightRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightServiceImpl flightService;

    @Test
    void create_shouldSaveFlight_whenCodeIsUniqueAndRouteExists() {
        // Arrange
        FlightCreateDTO dto = new FlightCreateDTO(
                "RO123", 1L,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                FlightStatus.SCHEDULED, "A1", null
        );

        Route route = new Route();
        route.setId(1L);

        Flight entityToSave = new Flight();
        Flight savedFlight = new Flight();
        savedFlight.setId(5L);

        FlightResponseDTO expectedResponse = new FlightResponseDTO(
                5L, "RO123", 1L, "Bucuresti", "OTP", "Paris", "CDG",
                dto.departureTime(), dto.arrivalTime(), "A1", null, FlightStatus.SCHEDULED
        );

        when(flightRepository.findByCode("RO123")).thenReturn(Optional.empty());
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(flightMapper.toEntity(dto)).thenReturn(entityToSave);
        when(flightRepository.save(entityToSave)).thenReturn(savedFlight);
        when(flightMapper.toResponse(savedFlight)).thenReturn(expectedResponse);

        // Act
        FlightResponseDTO result = flightService.create(dto);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(entityToSave.getRoute()).isEqualTo(route);
        assertThat(entityToSave.getGate()).isEqualTo("A1");
        // dto.aircraftId() e null, deci aircraftRepository nu ar trebui interogat deloc.
        verify(aircraftRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrowConflictException_whenFlightCodeAlreadyExists() {
        // Arrange
        FlightCreateDTO dto = new FlightCreateDTO(
                "RO123", 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                FlightStatus.SCHEDULED, null, null
        );
        when(flightRepository.findByCode("RO123")).thenReturn(Optional.of(new Flight()));

        // Act + Assert
        assertThatThrownBy(() -> flightService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("RO123");

        verify(routeRepository, never()).findById(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenFlightDoesNotExist() {
        // Arrange
        when(flightRepository.existsById(1L)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> flightService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(flightRepository, never()).deleteById(any());
    }

    @Test
    void register_shouldThrowConflictException_whenEmailAlreadyRegistered() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "existent@test.com", "0722000000", "parola123",
                "PASSPORT", "AB1234", "RO", null, null
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("existent@test.com");
    }

    // @ExtendWith(MockitoExtension.class) porneste Mockito pentru aceasta clasa de test:
// initializeaza automat campurile marcate cu @Mock si @InjectMocks, fara a porni
// contextul Spring. Acesta e motivul pentru care testul ruleaza in milisecunde.
    @ExtendWith(MockitoExtension.class)
    class AircraftServiceImplTest {

        // @Mock creeaza o implementare "falsa" a interfetei AircraftRepository.
        // Nu atinge baza de date - noi ii spunem exact ce sa returneze la fiecare apel.
        @Mock
        private AircraftRepository aircraftRepository;

        @Mock
        private AircraftMapper aircraftMapper;

        // @InjectMocks creeaza o instanta REALA de AircraftServiceImpl si ii injecteaza
        // cele doua mock-uri de mai sus (prin constructorul generat de @RequiredArgsConstructor).
        // Asadar logica din AircraftServiceImpl ruleaza normal; doar dependintele sunt false.
        @InjectMocks
        private AircraftServiceImpl aircraftService;

        @Test
        void create_shouldSaveAircraft_whenTailNumberIsUnique() {
            // ---------- Arrange ----------
            // Pregatim datele de intrare si spunem mock-urilor ce sa raspunda.
            AircraftCreateDTO dto = new AircraftCreateDTO("YR-ABC", "Airbus A320", 180, "seatmap-a320", "ACTIVE");

            Aircraft entityToSave = new Aircraft();
            entityToSave.setTailNumber(dto.tailNumber());
            entityToSave.setModel(dto.model());
            entityToSave.setCapacity(dto.capacity());

            Aircraft savedEntity = new Aircraft();
            savedEntity.setId(1L);
            savedEntity.setTailNumber(dto.tailNumber());
            savedEntity.setModel(dto.model());
            savedEntity.setCapacity(dto.capacity());

            AircraftResponseDTO expectedResponse =
                    new AircraftResponseDTO(1L, dto.tailNumber(), dto.model(), dto.capacity(), dto.seatMapRef(), dto.status());

            // Nu exista deja un avion cu acest tail number -> create() trebuie sa continue normal.
            when(aircraftRepository.findByTailNumber(dto.tailNumber())).thenReturn(Optional.empty());
            when(aircraftMapper.toEntity(dto)).thenReturn(entityToSave);
            when(aircraftRepository.save(entityToSave)).thenReturn(savedEntity);
            when(aircraftMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

            // ---------- Act ----------
            // Apelam metoda reala pe care o testam.
            AircraftResponseDTO result = aircraftService.create(dto);

            // ---------- Assert ----------
            // Verificam rezultatul returnat...
            assertThat(result).isEqualTo(expectedResponse);
            // ...si, optional, ca repository-ul a fost intr-adevar apelat cu save().
            // Acest lucru confirma ca metoda nu s-a oprit mai devreme dintr-un alt motiv.
            verify(aircraftRepository).save(entityToSave);
        }
    }

    @Test
    void create_shouldThrowConflictException_whenTailNumberAlreadyExists() {
        // Arrange: repository-ul "gaseste" deja un avion cu acelasi tail number.
        AircraftCreateDTO dto = new AircraftCreateDTO("YR-ABC", "Airbus A320", 180, null, "ACTIVE");
        when(aircraftRepository.findByTailNumber(dto.tailNumber()))
                .thenReturn(Optional.of(new Aircraft()));

        // Act + Assert: verificam ca exceptia asteptata este aruncata, cu mesajul corect.
        assertThatThrownBy(() -> aircraftService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("YR-ABC");

        // Important: daca s-a aruncat exceptia inainte de save(), inseamna ca save()
        // NU trebuie sa fi fost apelat niciodata. Verificam si asta.
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenAircraftDoesNotExist() {
        // Arrange
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> aircraftService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // Nota: metodele list()/listForPassenger() se bazeaza pe Specification<Flight> (JPA Criteria API),
    // care are sens sa fie verificat printr-un test de integrare cu o baza de date reala (ex: @DataJpaTest),
    // nu printr-un unit test cu mock-uri. Aici ne concentram pe metodele cu logica de business "pura".
    @ExtendWith(MockitoExtension.class)
    class FlightServiceImplTest {

}
