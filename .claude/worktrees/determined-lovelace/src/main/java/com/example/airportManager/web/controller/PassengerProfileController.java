package com.example.airportManager.web.controller;

import com.example.airportManager.dto.profile.PassengerProfileResponseDTO;
import com.example.airportManager.dto.profile.PassengerProfileUpdateDTO;
import com.example.airportManager.security.CustomUserDetails;
import com.example.airportManager.service.PassengerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passenger/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PASSENGER')")
public class PassengerProfileController {

    private final PassengerProfileService passengerProfileService;

    @GetMapping("/me")
    public ResponseEntity<PassengerProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(passengerProfileService.getMyProfile(userDetails.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<PassengerProfileResponseDTO> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PassengerProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(passengerProfileService.updateMyProfile(userDetails.getUserId(), dto));
    }
}
