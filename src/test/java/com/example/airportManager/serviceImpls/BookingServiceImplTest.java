package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.booking.BookingCreateDTO;
import com.example.airportManager.dto.booking.BookingResponseDTO;
import com.example.airportManager.mapper.BookingMapper;
import com.example.airportManager.model.*;
import com.example.airportManager.repository.BookingRepository;
import com.example.airportManager.repository.FlightRepository;
import com.example.airportManager.repository.PassengerProfileRepository;
import com.example.airportManager.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerProfileRepository passengerProfileRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Flight flight;

    private Route route;

    private Aircraft aircraft;

    private Airport airportDep;

    private Airport airportArr;

    private BookingCreateDTO bookingCreateDTO;

    List<PassengerProfile> allPassengers = new ArrayList<>();

    @BeforeEach
    void setUp() {
//      aircraft ----- done

        aircraft = new Aircraft();
        aircraft.setId(1L);
        aircraft.setTailNumber("YR-ABC");
        aircraft.setModel("Airbus A320");
        aircraft.setCapacity(2);
        aircraft.setSeatMapRef("A320-STD");
        aircraft.setStatus("ACTIVE");

//      two airports - for route ------------ done

        airportDep = new Airport();
        airportDep.setId(1L);
        airportDep.setIata("OTP");
        airportDep.setIcao("LROP");
        airportDep.setName("Henri Coanda International");
        airportDep.setCity("Bucharest");
        airportDep.setCountry("Romania");
        airportDep.setTimezone("Europe/Bucharest");

        airportArr = new Airport();
        airportArr.setId(2L);
        airportArr.setIata("TSR");
        airportArr.setIcao("LRTR");
        airportArr.setName("Traian Vuia International Airport");
        airportArr.setCity("timisoara");
        airportArr.setCountry("Romania");
        airportArr.setTimezone("Europe/Bucharest");

        //      route - for flight ------------- done

        route = new Route();
        route.setId(1L);
        route.setDistanceNm(200);
        route.setStdDurationMin(60);
        route.setOriginAirport(airportDep);
        route.setDestAirport(airportArr);

        //      flight -------------------------- done

        flight = new Flight();
        flight.setId(1L);
        flight.setCode("123");
        flight.setAircraft(aircraft);
        flight.setRoute(route);
        flight.setStatus(FlightStatus.SCHEDULED);
        flight.setDepartureScheduled(LocalDateTime.now().plusDays(1));
        flight.setArrivalScheduled(LocalDateTime.now().plusDays(1).plusHours(1));

        //      passengers -------------------- done

        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setEmail("pass" + i + "@test.com");
            user.setPhone("070000000" + i);
            user.setStatus(UserStatus.ACTIVE);
            UUID userId = UUID.randomUUID();
            user.setId(userId);

            PassengerProfile passenger = new PassengerProfile();
            passenger.setUserId(userId);
            passenger.setUser(user);
            passenger.setNationality("RO");
            passenger.setDocNumber("DOC" + i);
            passenger.setDocType("National ID");
            passenger.setUserId(user.getId());

            allPassengers.add(passenger);
        }
        bookingCreateDTO = new BookingCreateDTO(flight.getId());
    }

//  create(BookingCreateDTO, UUID) — happy path assigns a PNR and CONFIRMED status

//  DONE

    @Test
    void createWorksHappyPath() {
        when(passengerProfileRepository
                .findById(allPassengers.get(0).getUserId()))
                .thenReturn(Optional.ofNullable(allPassengers.get(0)));
        when(flightRepository.findById(flight.getId())).thenReturn(Optional.ofNullable(flight));
        when(bookingRepository.countConfirmedByFlightId(flight.getId())).thenReturn(0L);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Booking b = invocationOnMock.getArgument(0);
                        return new BookingResponseDTO(
                                b.getId(),
                                b.getPnr(),
                                allPassengers.get(0).getUserId(),
                                flight.getId(),
                                b.getStatus()
                        );
                    }
                }
        );
        BookingResponseDTO result = bookingService.create(bookingCreateDTO, allPassengers.get(0).getUserId());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(saved.getPnr()).matches("PNR[A-F0-9]{8}");
        assertThat(saved.getPassengerProfile()).isEqualTo(allPassengers.get(0));
        assertThat(saved.getFlight()).isEqualTo(flight);

        assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.pnr()).matches("PNR[A-F0-9]{8}");
    }

//   create —  ⚠️ [MAJOR] Booking capacity check has a TOCTOU race,
//   and a flight with no aircraft can never be booked #44:
//   a flight with a null aircraft produces a distinct
//   "no aircraft assigned" error, not "fully booked". Fails today.

//   DONE - without TOCTOU

    @Test
    void createFlightWithoutAircraftGivesNoAircraftAssigned(){
        flight.setAircraft(null);

        when(passengerProfileRepository
                .findById(allPassengers.get(0).getUserId()))
                .thenReturn(Optional.ofNullable(allPassengers.get(0)));
        when(flightRepository.findById(flight.getId())).thenReturn(Optional.ofNullable(flight));

        assertThatThrownBy(() -> bookingService.create(bookingCreateDTO,allPassengers.get(0).getUserId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No aircraft is assigned")
                .hasMessageNotContaining("fully booked");

        verify(bookingRepository, never()).save(any());
    }

//  create — at capacity, throws; at capacity minus one, succeeds (boundary test)
//  DONE

    @Test
    void createThrowsAtCapacity(){
        when(passengerProfileRepository
                .findById(allPassengers.get(0).getUserId()))
                .thenReturn(Optional.ofNullable(allPassengers.get(0)));
        when(flightRepository.findById(flight.getId())).thenReturn(Optional.ofNullable(flight));
        when(bookingRepository.countConfirmedByFlightId(flight.getId())).thenReturn((long) aircraft.getCapacity());

        assertThatThrownBy(() -> bookingService.create(bookingCreateDTO,allPassengers.get(0).getUserId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fully booked");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createSucceedsAtCapacityMinusOne(){
        when(passengerProfileRepository
                .findById(allPassengers.get(0).getUserId()))
                .thenReturn(Optional.ofNullable(allPassengers.get(0)));
        when(flightRepository.findById(flight.getId())).thenReturn(Optional.ofNullable(flight));
        when(bookingRepository.countConfirmedByFlightId(flight.getId())).thenReturn((long) (aircraft.getCapacity()-1));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(
                new Answer() {

                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Booking b = invocationOnMock.getArgument(0);
                        return new BookingResponseDTO(
                                b.getId(),
                                b.getPnr(),
                                allPassengers.get(0).getUserId(),
                                flight.getId(),
                                b.getStatus()
                        );
                    }
                }
        );

        BookingResponseDTO result = bookingService.create(bookingCreateDTO, allPassengers.get(0).getUserId());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
    }
//  getMyBookings(UUID) — returns only the caller's bookings

//  DONE

    private Booking booking_UUIDTest(Long id, String pnr, PassengerProfile owner) {
        Booking b = new Booking();
        b.setId(id);
        b.setPnr(pnr);
        b.setPassengerProfile(owner);
        b.setFlight(flight);
        b.setStatus(BookingStatus.CONFIRMED);
        return b;
    }

    @Test
    void getMyBookings_returnOnlyCallersBookings(){
        PassengerProfile caller = allPassengers.get(0);
        PassengerProfile other = allPassengers.get(1);

        Booking booking1Caller = booking_UUIDTest(1L, "PNR-Caller-1", caller);
        Booking booking2Caller = booking_UUIDTest(2L, "PNR-Caller-2", caller);
        Booking bookingOther = booking_UUIDTest(3L, "PNR-Other", caller);

        when(bookingRepository.findByPassengerProfileUserId(caller.getUserId()))
                .thenReturn(List.of(booking1Caller, booking2Caller));
        when(bookingMapper.toResponse(any(Booking.class)))
                .thenAnswer(invocation ->
                        {
                            Booking b = invocation.getArgument(0);
                            return new BookingResponseDTO(
                                    b.getId(),
                                    b.getPnr(),
                                    allPassengers.get(0).getUserId(),
                                    flight.getId(),
                                    b.getStatus()
                                    );
                        });
        List<BookingResponseDTO> result = bookingService.getMyBookings(caller.getUserId());

        assertThat(result.get(0).pnr()).contains("Caller");
        assertThat(result.get(1).pnr()).contains("Caller");
        assertThat(result.get(0).pnr()).doesNotContain("Other");
        assertThat(result.get(1).pnr()).doesNotContain("Other");

        verify(bookingRepository, never()).findByPassengerProfileUserId(other.getUserId());
        verify(bookingMapper, never()).toResponse(bookingOther);
    }

//  cancel(Long, UUID) — owner can cancel; non-owner cannot; an already-cancelled booking is rejected

    @Test
    void cancel_OwnerCanCancel(){
        PassengerProfile owner = allPassengers.get(0);
        Booking b = booking_UUIDTest(1L, "PNR-Owner", owner);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(invocation ->
                {
                    Booking cancelled = invocation.getArgument(0);
                    return new BookingResponseDTO(
                            cancelled.getId(),
                            cancelled.getPnr(),
                            owner.getUserId(),
                            flight.getId(),
                            cancelled.getStatus()
                    );
                });
        BookingResponseDTO result = bookingService.cancel(1L, owner.getUserId());

        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(b.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(b);
    }

    @Test
    void cancel_NonOwnerCannotCancel(){
        PassengerProfile owner = allPassengers.get(0);
        PassengerProfile notOwner = allPassengers.get(1);
        Booking b = booking_UUIDTest(1L, "PNR-Owner", owner);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThatThrownBy(()->bookingService.cancel(1L, notOwner.getUserId()))
                .isInstanceOf(AccessDeniedException.class).
                hasMessageContaining("do not own");

        assertThat(b.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancel_AlreadyCancelledIsRejected(){
        PassengerProfile owner = allPassengers.get(0);
        Booking b = booking_UUIDTest(1L, "PNR-Owner", owner);
        b.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        assertThatThrownBy(() -> bookingService.cancel(1L, owner.getUserId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");

        verify(bookingRepository, never()).save(any());
    }
}

//        BookingServiceImpl (4 methods)
//
//        create — ⚠️ [MAJOR] Booking capacity check has a TOCTOU race, and a flight with no aircraft can never be booked #44: concurrency — N+1 simultaneous bookings against a flight of capacity N yield exactly N confirmed. Needs @DataJpaTest + real PostgreSQL, not Mockito.
//
//        getById(Long) — ⚠️ [CRITICAL] Broken Access Control: 13 endpoints across Airports, Routes, Passengers and Bookings enforce no role restriction #1: passenger A requesting passenger B's booking gets AccessDeniedException. Fails today (no ownership check exists).

