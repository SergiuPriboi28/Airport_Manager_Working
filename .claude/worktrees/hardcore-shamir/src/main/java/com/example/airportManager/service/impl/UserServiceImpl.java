package com.example.airportManager.service.impl;

import com.example.airportManager.dto.user.*;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.model.*;
import com.example.airportManager.repository.*;
import com.example.airportManager.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already registered: " + dto.email());
        }

        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : dto.roles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
            roles.add(role);
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        if (dto.roles().contains(RoleName.EMPLOYEE) && dto.position() != null && dto.department() != null && dto.hireDate() != null) {
            EmployeeProfile employeeProfile = new EmployeeProfile();
            employeeProfile.setUser(savedUser);
            employeeProfile.setPosition(dto.position());
            employeeProfile.setDepartment(dto.department());
            employeeProfile.setGrade(dto.grade());
            employeeProfile.setHireDate(dto.hireDate());
            employeeProfileRepository.save(employeeProfile);
        }

        if (dto.roles().contains(RoleName.PASSENGER) && dto.docType() != null && dto.docNumber() != null && dto.nationality() != null) {
            PassengerProfile passengerProfile = new PassengerProfile();
            passengerProfile.setUser(savedUser);
            passengerProfile.setDocType(dto.docType());
            passengerProfile.setDocNumber(dto.docNumber());
            passengerProfile.setNationality(dto.nationality());
            passengerProfile.setLoyaltyTier(dto.loyaltyTier());
            passengerProfile.setEmergencyContact(dto.emergencyContact());
            passengerProfileRepository.save(passengerProfile);
        }

        return toResponseDTO(userRepository.findById(savedUser.getId()).orElse(savedUser));
    }

    @Override
    @Transactional
    public UserResponseDTO update(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already registered: " + dto.email());
        }

        user.setEmail(dto.email());
        user.setPhone(dto.phone());

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        if (dto.status() != null) {
            user.setStatus(dto.status());
        }

        if (dto.roles() != null && !dto.roles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (RoleName roleName : dto.roles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);

        if (dto.roles() != null && dto.roles().contains(RoleName.EMPLOYEE)) {
            EmployeeProfile employeeProfile = employeeProfileRepository.findById(id).orElse(null);
            if (employeeProfile == null && dto.position() != null && dto.department() != null && dto.hireDate() != null) {
                employeeProfile = new EmployeeProfile();
                employeeProfile.setUser(savedUser);
            }
            if (employeeProfile != null) {
                if (dto.position() != null) employeeProfile.setPosition(dto.position());
                if (dto.department() != null) employeeProfile.setDepartment(dto.department());
                employeeProfile.setGrade(dto.grade());
                if (dto.hireDate() != null) employeeProfile.setHireDate(dto.hireDate());
                employeeProfileRepository.save(employeeProfile);
            }
        }

        if (dto.roles() != null && dto.roles().contains(RoleName.PASSENGER)) {
            PassengerProfile passengerProfile = passengerProfileRepository.findById(id).orElse(null);
            if (passengerProfile == null && dto.docType() != null && dto.docNumber() != null && dto.nationality() != null) {
                passengerProfile = new PassengerProfile();
                passengerProfile.setUser(savedUser);
            }
            if (passengerProfile != null) {
                if (dto.docType() != null) passengerProfile.setDocType(dto.docType());
                if (dto.docNumber() != null) passengerProfile.setDocNumber(dto.docNumber());
                if (dto.nationality() != null) passengerProfile.setNationality(dto.nationality());
                passengerProfile.setLoyaltyTier(dto.loyaltyTier());
                passengerProfile.setEmergencyContact(dto.emergencyContact());
                passengerProfileRepository.save(passengerProfile);
            }
        }

        return toResponseDTO(userRepository.findById(savedUser.getId()).orElse(savedUser));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO toResponseDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        EmployeeProfileDTO employeeProfileDTO = null;
        if (user.getEmployeeProfile() != null) {
            EmployeeProfile ep = user.getEmployeeProfile();
            employeeProfileDTO = new EmployeeProfileDTO(
                    ep.getPosition(),
                    ep.getDepartment(),
                    ep.getGrade(),
                    ep.getHireDate()
            );
        }

        PassengerProfileDTO passengerProfileDTO = null;
        if (user.getPassengerProfile() != null) {
            PassengerProfile pp = user.getPassengerProfile();
            passengerProfileDTO = new PassengerProfileDTO(
                    pp.getDocType(),
                    pp.getDocNumber(),
                    pp.getNationality(),
                    pp.getLoyaltyTier(),
                    pp.getEmergencyContact()
            );
        }

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                employeeProfileDTO,
                passengerProfileDTO
        );
    }
}
