package com.example.airportManager.service.impl;

import com.example.airportManager.dto.profile.EmployeeProfileResponseDTO;
import com.example.airportManager.dto.profile.EmployeeProfileUpdateDTO;
import com.example.airportManager.model.EmployeeProfile;
import com.example.airportManager.model.User;
import com.example.airportManager.repository.EmployeeProfileRepository;
import com.example.airportManager.repository.UserRepository;
import com.example.airportManager.service.EmployeeProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfileResponseDTO getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee profile not found for user: " + userId));

        return toResponseDTO(user, profile);
    }

    @Override
    @Transactional
    public EmployeeProfileResponseDTO updateMyProfile(UUID userId, EmployeeProfileUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee profile not found for user: " + userId));

        profile.setPosition(dto.position());
        profile.setDepartment(dto.department());
        profile.setGrade(dto.grade());
        profile.setHireDate(dto.hireDate());

        EmployeeProfile savedProfile = employeeProfileRepository.save(profile);
        return toResponseDTO(user, savedProfile);
    }

    private EmployeeProfileResponseDTO toResponseDTO(User user, EmployeeProfile profile) {
        return new EmployeeProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                profile.getPosition(),
                profile.getDepartment(),
                profile.getGrade(),
                profile.getHireDate()
        );
    }
}
