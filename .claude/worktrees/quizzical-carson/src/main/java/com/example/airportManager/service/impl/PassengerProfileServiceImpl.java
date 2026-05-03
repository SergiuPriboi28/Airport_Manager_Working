package com.example.airportManager.service.impl;

import com.example.airportManager.dto.profile.PassengerProfileResponseDTO;
import com.example.airportManager.dto.profile.PassengerProfileUpdateDTO;
import com.example.airportManager.model.PassengerProfile;
import com.example.airportManager.model.User;
import com.example.airportManager.repository.PassengerProfileRepository;
import com.example.airportManager.repository.UserRepository;
import com.example.airportManager.service.PassengerProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassengerProfileServiceImpl implements PassengerProfileService {

    private final PassengerProfileRepository passengerProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PassengerProfileResponseDTO getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        PassengerProfile profile = passengerProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger profile not found for user: " + userId));

        return toResponseDTO(user, profile);
    }

    @Override
    @Transactional
    public PassengerProfileResponseDTO updateMyProfile(UUID userId, PassengerProfileUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        PassengerProfile profile = passengerProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger profile not found for user: " + userId));

        profile.setDocType(dto.docType());
        profile.setDocNumber(dto.docNumber());
        profile.setNationality(dto.nationality());
        profile.setLoyaltyTier(dto.loyaltyTier());
        profile.setEmergencyContact(dto.emergencyContact());

        PassengerProfile savedProfile = passengerProfileRepository.save(profile);
        return toResponseDTO(user, savedProfile);
    }

    private PassengerProfileResponseDTO toResponseDTO(User user, PassengerProfile profile) {
        return new PassengerProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                profile.getDocType(),
                profile.getDocNumber(),
                profile.getNationality(),
                profile.getLoyaltyTier(),
                profile.getEmergencyContact()
        );
    }
}
