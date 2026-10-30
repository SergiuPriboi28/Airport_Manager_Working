package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.route.RouteCreateDTO;
import com.example.airportManager.dto.route.RouteResponseDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.mapper.RouteMapper;
import com.example.airportManager.model.Aircraft;
import com.example.airportManager.model.Airport;
import com.example.airportManager.model.Route;
import com.example.airportManager.repository.AirportRepository;
import com.example.airportManager.repository.RouteRepository;
import com.example.airportManager.service.impl.RouteServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.postgresql.hostchooser.HostRequirement.any;


@ExtendWith(MockitoExtension.class)
public class RouteServiceImplTest {
    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteMapper routeMapper;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Aircraft aircraft;
    private Airport airportDep;
    private Airport airportArr;
    private Route route;
    private RouteResponseDTO responseDTO;
    private RouteCreateDTO createDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {

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

        route = new Route();
        route.setId(10L);
        route.setDistanceNm(200);
        route.setStdDurationMin(60);
        route.setDestAirport(airportArr);
        route.setOriginAirport(airportDep);

        responseDTO = new RouteResponseDTO(10L, 1L, 2L, 200, 60);
        createDTO = new RouteCreateDTO(1L, 2L, 200, 60);
        pageable = PageRequest.of(0, 10);
    }

    //    list(Pageable, Optional<Long>, Optional<Long>) — both filters applied, and the neither-supplied case

    @Test
    void list_returnsFilteredPage_whenBothFiltersApplied(){
        Page<Route> page = new PageImpl<>(List.of(route), pageable, 1);
        when(routeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(routeMapper.toResponse(route)).thenReturn(responseDTO);

        Page<RouteResponseDTO> result = routeService.list(pageable, Optional.of(1L), Optional.of(2L));

        assertThat(result.getContent()).containsExactly(responseDTO);

        ArgumentCaptor<Specification<Route>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(routeRepository).findAll(captor.capture(), eq(pageable));
        Specification<Route> spec = captor.getValue();

        Root<Route> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> originPath = mock(Path.class);
        Path<Object> originIdPath = mock(Path.class);
        Path<Object> destPath = mock(Path.class);
        Path<Object> destIdPath = mock(Path.class);
        Predicate originPredicate = mock(Predicate.class);
        Predicate destPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(root.get("originAirport")).thenReturn(originPath);
        when(originPath.get("id")).thenReturn(originIdPath);
        when(root.get("destAirport")).thenReturn(destPath);
        when(destPath.get("id")).thenReturn(destIdPath);
        when(cb.equal(originIdPath,1L)).thenReturn(originPredicate);
        when(cb.equal(destIdPath,2L)).thenReturn(destPredicate);
        when(cb.and(originPredicate, destPredicate)).thenReturn(combinedPredicate);

        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isSameAs(combinedPredicate);
        verify(cb).equal(originIdPath, 1L);
        verify(cb).equal(destIdPath, 2L);
        verify(cb).and(originPredicate, destPredicate);
    }

    //    list(Pageable, Optional<Long>, Optional<Long>) — both filters applied, and the neither-supplied case

    @Test
    void list_returnsPage_whenNeitherFilterApplied(){
        Page<Route> page = new PageImpl<>(List.of(route), pageable, 1);
        when(routeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(routeMapper.toResponse(route)).thenReturn(responseDTO);

        Page<RouteResponseDTO> result = routeService.list(pageable, Optional.empty(), Optional.empty());

        assertThat(result.getContent()).containsExactly(responseDTO);

        ArgumentCaptor<Specification<Route>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(routeRepository).findAll(captor.capture(), eq(pageable));
        Specification<Route> spec = captor.getValue();

        Root<Route> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate predicate = spec.toPredicate(root, query, cb);

        assertThat(predicate).isNull();
        verifyNoInteractions(root, cb);
    }

//    create(RouteCreateDTO) — ⚠️ the @DifferentAirports rule rejects origin == destination

    @Test
    void create_HappyPath(){
        when(routeRepository.existsByOriginAirportIdAndDestAirportId(1L,2L)).thenReturn(false);
        when(airportRepository.findById(1L)).thenReturn(Optional.ofNullable(airportDep));
        when(airportRepository.findById(2L)).thenReturn(Optional.ofNullable(airportArr));
        when(routeMapper.toEntity(createDTO)).thenReturn(route);
        when(routeRepository.save(route)).thenReturn(route);
        when(routeMapper.toResponse(route)).thenReturn(responseDTO);

        RouteResponseDTO result = routeService.create(createDTO);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(result.originAirportId()).isEqualTo(airportDep.getId());
        assertThat(result.destAirportId()).isEqualTo(airportArr.getId());
        verify(routeRepository).save(route);
    }

    @Test
    void create_FailsWhenOriginEqualsDestination(){
        RouteCreateDTO sameDepAndArrDTO = new RouteCreateDTO(1L, 1L, 200, 60);

        assertThatThrownBy(()-> routeService.create(sameDepAndArrDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("same origin and destination");

        verify(routeRepository, never()).save(any());
    }

    @Test
    void create_FailsWhenRouteExistsAlready(){
        when(routeRepository.existsByOriginAirportIdAndDestAirportId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> routeService.create(createDTO))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(routeRepository, never()).save(any());
    }

    @Test
    void create_FailsWhenAirportDoesNotExist(){
        when(routeRepository.existsByOriginAirportIdAndDestAirportId(1L,2L)).thenReturn(false);
        when(airportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("airport not found");

        verify(routeRepository, never()).save(any());
    }

    //    delete(Long) — a route referenced by flights

    @Test
    void delete_happyPath(){
        when(routeRepository.existsById(1L)).thenReturn(true);

        routeService.delete(1L);

        verify(routeRepository).deleteById(1L);
    }

    @Test
    void delete_RouteDoesNotExist(){
        when(routeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> routeService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(routeRepository, never()).deleteById(any());
    }

    @Test
    void delete_referencedByFlight_propagatesDataIntegrityViolationException() {
        when(routeRepository.existsById(10L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Referenced by flight"))
                .when(routeRepository).deleteById(10L);

        assertThatThrownBy(() -> routeService.delete(10L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}
