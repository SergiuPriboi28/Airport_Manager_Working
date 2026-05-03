package com.example.airportManager.web.controller;

import com.example.airportManager.dto.profile.EmployeeProfileResponseDTO;
import com.example.airportManager.dto.profile.EmployeeProfileUpdateDTO;
import com.example.airportManager.security.CustomUserDetails;
import com.example.airportManager.service.EmployeeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(employeeProfileService.getMyProfile(userDetails.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileResponseDTO> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmployeeProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(employeeProfileService.updateMyProfile(userDetails.getUserId(), dto));
    }
}
