package com.example.airportManager.serviceImpls;

import com.example.airportManager.dto.user.UserCreateDTO;
import com.example.airportManager.dto.user.UserResponseDTO;
import com.example.airportManager.dto.user.UserUpdateDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.model.EmployeeProfile;
import com.example.airportManager.model.PassengerProfile;
import com.example.airportManager.model.Role;
import com.example.airportManager.model.RoleName;
import com.example.airportManager.model.User;
import com.example.airportManager.model.UserStatus;
import com.example.airportManager.repository.EmployeeProfileRepository;
import com.example.airportManager.repository.PassengerProfileRepository;
import com.example.airportManager.repository.RoleRepository;
import com.example.airportManager.repository.UserRepository;
import com.example.airportManager.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeProfileRepository employeeProfileRepository;

    @Mock
    private PassengerProfileRepository passengerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User userPassenger;
    private User userEmployee;
    private UUID passUserID;
    private UUID emplUserID;
    private Role employeeRole;
    private Role passengerRole;

    @BeforeEach
    void setUp(){
        employeeRole = new Role(1L, RoleName.EMPLOYEE);
        passengerRole = new Role(1L, RoleName.PASSENGER);

        userPassenger = new User();
        passUserID = UUID.randomUUID();
        userPassenger.setId(passUserID);
        userPassenger.setStatus(UserStatus.ACTIVE);
        userPassenger.setEmail("passenger@example.com");
        userPassenger.setPhone("1234567890");
        userPassenger.setPasswordHash("passenger-hash");
        userPassenger.setRoles(Set.of(passengerRole));

        userEmployee = new User();
        emplUserID = UUID.randomUUID();
        userEmployee.setId(emplUserID);
        userEmployee.setStatus(UserStatus.ACTIVE);
        userEmployee.setEmail("employee@example.com");
        userEmployee.setPhone("0987654321");
        userEmployee.setPasswordHash("employee-hash");
        userEmployee.setRoles(Set.of(employeeRole));
    }

    private static final LocalDate HIRE_DATE = LocalDate.of(2024, 3, 1);

    UserCreateDTO employeeCreateDTO = new UserCreateDTO(
            "emp@example.com", "+40711111111", "password123",
            Set.of(RoleName.EMPLOYEE),
            "Gate Agent", "Operations", "G3", HIRE_DATE,
            null, null, null, null, null
    );

    UserCreateDTO passengerCreateDTO = new UserCreateDTO(
            "pax@example.com", "+40722222222", "password123",
            Set.of(RoleName.PASSENGER),
            null, null, null, null,
            "PASSPORT", "X1234567",
            "RO", "GOLD", "+40733333333"
    );

    UserUpdateDTO updateDTO = new UserUpdateDTO(
            "updated@example.com", "+40700000000",
             "updated-hash", null, null,
            null, null, null, null,
            null, null, null, null, null
    );


//    listAll(Pageable)
//
    @Test
    void listAll_returnsMappedPage_respectingPageSize(){
        Pageable pageable = PageRequest.of(0,2);
        Page<User> page = new PageImpl<>(List.of(userPassenger,userEmployee), pageable, 5);
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserResponseDTO> result = userService.listAll(pageable);

        assertThat(result.getContent()).extracting(UserResponseDTO::email)
                .containsExactly("passenger@example.com", "employee@example.com");

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getSize()).isEqualTo(2);
        verify(userRepository).findAll(pageable);
    }

//    getById(UUID) — found and not-found
//
    @Test
    void getById_found(){
        when(userRepository.findById(passUserID)).thenReturn(Optional.ofNullable(userPassenger));

        UserResponseDTO result = userService.getById(passUserID);

        assertThat(result.email()).isEqualTo("passenger@example.com");
        assertThat(result.id()).isEqualTo(passUserID);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.roles()).containsExactly("PASSENGER");
        assertThat(result.phone()).isEqualTo("1234567890");
    }

    @Test
    void getById_NOTFound(){
        when(userRepository.findById(passUserID)).thenReturn(Optional.empty());

        assertThatThrownBy(()->userService.getById(passUserID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

//    create(UserCreateDTO) — password encoded;
//    ⚠️ [MINOR] UserServiceImpl mixes responsibilities, duplicates logic,
//    and omits created profiles from its response
//    #24: the created PassengerProfile / EmployeeProfile appears in the response. Fails today.

    UUID USER_ID = UUID.randomUUID();

    private AtomicReference<User> stubPersistenceContext() {
        AtomicReference<User> managed = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            managed.set(u);
            return u;
        });
        when(userRepository.findById(USER_ID)).thenAnswer(inv -> Optional.of(managed.get()));
        return managed;
    }

    @Test
    void create_encodesPassword(){
        stubPersistenceContext();
        when(userRepository.existsByEmail("emp@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        userService.create(employeeCreateDTO);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPasswordHash())
                .isEqualTo("hashedPassword").isNotEqualTo("password123");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void create_whenEmailAlreadyRegistered_throwsConflict_andSavesNothing() {
        when(userRepository.existsByEmail("emp@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(employeeCreateDTO))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

//    create — the PASSENGER branch and the EMPLOYEE branch create the right profile type
//
    @Test
    void create_passengerBranch_createsPassengerProfileOnly(){
        stubPersistenceContext();
        when(roleRepository.findByName(RoleName.PASSENGER)).thenReturn(Optional.of(passengerRole));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        userService.create(passengerCreateDTO);

        ArgumentCaptor<PassengerProfile> captor = ArgumentCaptor.forClass(PassengerProfile.class);
        verify(passengerProfileRepository).save(captor.capture());
        PassengerProfile profile = captor.getValue();

        assertThat(profile.getUser().getId()).isEqualTo(USER_ID);
        assertThat(profile.getUser().getId()).isEqualTo(USER_ID);
        assertThat(profile.getDocType()).isEqualTo("PASSPORT");
        assertThat(profile.getDocNumber()).isEqualTo("X1234567");
        assertThat(profile.getNationality()).isEqualTo("RO");
        assertThat(profile.getLoyaltyTier()).isEqualTo("GOLD");
        assertThat(profile.getEmergencyContact()).isEqualTo("+40733333333");
        verifyNoInteractions(employeeProfileRepository);
    }

    @Test
    void create_employeeBranch_createsEmployeeProfileOnly() {
        stubPersistenceContext();
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-hash");

        userService.create(employeeCreateDTO);

        ArgumentCaptor<EmployeeProfile> captor = ArgumentCaptor.forClass(EmployeeProfile.class);
        verify(employeeProfileRepository).save(captor.capture());
        EmployeeProfile profile = captor.getValue();
        assertThat(profile.getUser().getId()).isEqualTo(USER_ID);
        assertThat(profile.getPosition()).isEqualTo("Gate Agent");
        assertThat(profile.getDepartment()).isEqualTo("Operations");
        assertThat(profile.getGrade()).isEqualTo("G3");
        assertThat(profile.getHireDate()).isEqualTo(HIRE_DATE);
        verifyNoInteractions(passengerProfileRepository);
    }

    @Test
    void create_employee_responseIncludesEmployeeProfile() {
        stubPersistenceContext();
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-hash");

        UserResponseDTO response = userService.create(employeeCreateDTO);

        assertThat(response.employeeProfile()).isNotNull();
        assertThat(response.employeeProfile().position()).isEqualTo("Gate Agent");
        assertThat(response.employeeProfile().department()).isEqualTo("Operations");
        assertThat(response.employeeProfile().hireDate()).isEqualTo(HIRE_DATE);
    }

    @Test
    void create_passenger_responseIncludesPassengerProfile() {
        stubPersistenceContext();
        when(roleRepository.findByName(RoleName.PASSENGER)).thenReturn(Optional.of(passengerRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-hash");

        UserResponseDTO response = userService.create(passengerCreateDTO);

        assertThat(response.passengerProfile()).isNotNull();
        assertThat(response.passengerProfile().docNumber()).isEqualTo("X1234567");
        assertThat(response.passengerProfile().nationality()).isEqualTo("RO");
    }

//    update(UUID, UserUpdateDTO) — ⚠️ [MAJOR] UserUpdateDTO.password is
//    missing the length validation that UserCreateDTO has
//    #40: a 1-character password is rejected. Fails today.
//
//    update — a null password leaves the existing hash untouched rather than nulling it
//
//    delete(UUID) — cascade behaviour on the associated profile


}
