package com.example.airportManager.service.impl;

import com.example.airportManager.dto.auth.AuthResponseDTO;
import com.example.airportManager.dto.auth.GoogleLoginRequestDTO;
import com.example.airportManager.dto.auth.LoginRequestDTO;
import com.example.airportManager.dto.auth.RegisterRequestDTO;
import com.example.airportManager.exception.ConflictException;
import com.example.airportManager.exception.InvalidOAuthTokenException;
import com.example.airportManager.model.*;
import com.example.airportManager.repository.PassengerProfileRepository;
import com.example.airportManager.repository.RoleRepository;
import com.example.airportManager.repository.UserRepository;
import com.example.airportManager.security.CustomUserDetails;
import com.example.airportManager.security.JwtUtil;
import com.example.airportManager.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${google.client.id}")
    private String googleClientID;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registration attempt: email={}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email={} already registered", request.email());
            throw new ConflictException("Email already registered: " + request.email());
        }

        Role passengerRole = roleRepository.findByName(RoleName.PASSENGER)
                .orElseThrow(() -> new IllegalStateException("PASSENGER role not found"));

        User user = new User();
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(passengerRole));

        User savedUser = userRepository.save(user);

        PassengerProfile passengerProfile = new PassengerProfile();
        passengerProfile.setUser(savedUser);
        passengerProfile.setDocType(request.docType());
        passengerProfile.setDocNumber(request.docNumber());
        passengerProfile.setNationality(request.nationality());
        passengerProfile.setLoyaltyTier(request.loyaltyTier());
        passengerProfile.setEmergencyContact(request.emergencyContact());

        passengerProfileRepository.save(passengerProfile);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        Set<String> roles = savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        log.info("User registered: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        return new AuthResponseDTO(savedUser.getId(), savedUser.getEmail(), roles, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            log.warn("Login failed: email={}, reason={}", request.email(), ex.getClass().getSimpleName());
            throw ex;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        String token = jwtUtil.generateToken(userDetails);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        log.info("Login succeeded: userId={}, email={}", user.getId(), user.getEmail());
        return new AuthResponseDTO(user.getId(), user.getEmail(), roles, token);
    }

    @Override
    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientID)).build();

        GoogleIdToken idToken;

        try{
            idToken = verifier.verify(request.idToken());
        } catch (Exception e) {
            log.warn("Google login failed: token verification error, reason={}", e.getClass().getSimpleName());
            throw new RuntimeException("Google verification token has failed!");
        }
        if (StringUtils.isEmpty(String.valueOf(idToken))) {
            log.warn("Google login failed: empty token");
            throw new RuntimeException("Google token is empty!");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleID = payload.getSubject();
        String email = payload.getEmail();
        String firstName = payload.get("given_name").toString();
        String lastName = payload.get("family_name").toString();
        User userGoogle = resolveUser(googleID,email,firstName,lastName);
        CustomUserDetails customUserDetails = new CustomUserDetails(userGoogle);
        String token = jwtUtil.generateToken(customUserDetails);

        Set<String> roles = userGoogle.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        log.info("Google login succeeded: userId={}, email={}", userGoogle.getId(), userGoogle.getEmail());
        return new AuthResponseDTO(userGoogle.getId(), userGoogle.getEmail(), roles, token);
    }

    private User resolveUser(String googleId, String email, String firstName, String lastName){
        Optional<User> byGoogleId = userRepository.findByGoogleId(googleId);
        if (byGoogleId.isPresent()) return byGoogleId.get();

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()){
            User existing = byEmail.get();
            existing.setGoogleId(googleId);
            return userRepository.save(existing);
        }

        Role defaultRole = roleRepository.findByName(RoleName.PASSENGER).orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .email(email)
                .passwordHash(null)
                .googleId(googleId)
                .role(RoleName.PASSENGER)
                .roles(Set.of(defaultRole)).build();

        return userRepository.save(user);
    }
}
