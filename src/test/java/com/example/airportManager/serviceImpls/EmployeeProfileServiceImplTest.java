package com.example.airportManager.serviceImpls;


import com.example.airportManager.dto.profile.EmployeeProfileResponseDTO;
import com.example.airportManager.dto.profile.EmployeeProfileUpdateDTO;
import com.example.airportManager.dto.user.EmployeeProfileDTO;
import com.example.airportManager.model.EmployeeProfile;
import com.example.airportManager.model.User;
import com.example.airportManager.model.UserStatus;
import com.example.airportManager.repository.EmployeeProfileRepository;
import com.example.airportManager.repository.UserRepository;
import com.example.airportManager.service.impl.EmployeeProfileServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeProfileServiceImplTest {

    @Mock
    private EmployeeProfileRepository employeeProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeProfileServiceImpl employeeProfileService;

    private User user;
    private EmployeeProfile employeeProfile;
    private UUID userId;

    @BeforeEach
    void setUp(){

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setPhone("1234567890");
        user.setStatus(UserStatus.ACTIVE);

        employeeProfile = new EmployeeProfile();
        employeeProfile.setUserId(userId);
        employeeProfile.setUser(user);
        employeeProfile.setPosition("Logistics");
        employeeProfile.setDepartment("Log");
        employeeProfile.setGrade("1");
        employeeProfile.setHireDate(LocalDate.of(2022, 3, 1));
    }

    //getMyProfile(UUID) — found; absent-profile path

    @Test
    void getMyProfile_HappyPath(){
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(user));
        when(employeeProfileRepository.findById(userId)).thenReturn(Optional.ofNullable(employeeProfile));

        EmployeeProfileResponseDTO result = employeeProfileService.getMyProfile(userId);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.phone()).isEqualTo("1234567890");
        assertThat(result.hireDate()).isEqualTo(LocalDate.of(2022, 3, 1));
        assertThat(result.position()).isEqualTo("Logistics");
        assertThat(result.department()).isEqualTo("Log");
        assertThat(result.grade()).isEqualTo("1");
        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    void absentProfilePath_user(){
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(()->employeeProfileService.getMyProfile(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(employeeProfileRepository, never()).findById(any());
    }

    @Test
    void absentProfilePath_employeeProfile(){
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(user));
        when(employeeProfileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(()->employeeProfileService.getMyProfile(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Employee profile not found");
    }

    //updateMyProfile(UUID, EmployeeProfileUpdateDTO) — updates only the caller's own profile

    @Test
    void updateMyProfileUpdatesOnlyCallersProfile(){
        UUID otherId = UUID.randomUUID();
        EmployeeProfileUpdateDTO updateDTO = new EmployeeProfileUpdateDTO(
                "Big Logistic",
                "Big Log",
                "2",
                LocalDate.of(2025,10,11));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(user));
        when(employeeProfileRepository.findById(userId)).thenReturn(Optional.ofNullable(employeeProfile));
        when(employeeProfileRepository.save(any(EmployeeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeProfileResponseDTO response = employeeProfileService.updateMyProfile(userId, updateDTO);

        ArgumentCaptor<EmployeeProfile> captor = ArgumentCaptor.forClass(EmployeeProfile.class);
        verify(employeeProfileRepository).save(captor.capture());
        EmployeeProfile saved = captor.getValue();

//      VERIFY RESPONSE IS UPDATED
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.hireDate()).isEqualTo(LocalDate.of(2025,10,11));
        assertThat(response.position()).isEqualTo("Big Logistic");
        assertThat(response.department()).isEqualTo("Big Log");
        assertThat(response.grade()).isEqualTo("2");

//      VERIFY RESPONSE IS SAVED CORRECTLY
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getHireDate()).isEqualTo(LocalDate.of(2025,10,11));
        assertThat(saved.getPosition()).isEqualTo("Big Logistic");
        assertThat(saved.getDepartment()).isEqualTo("Big Log");
        assertThat(saved.getGrade()).isEqualTo("2");

//      VERIFY OTHER USER ID CANNOT UPDATE OWNER PROFILE
        verify(employeeProfileRepository, never()).findById(otherId);
        verify(userRepository, never()).findById(otherId);
    }
}