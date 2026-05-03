package com.example.airportManager.service;

import com.example.airportManager.dto.user.UserCreateDTO;
import com.example.airportManager.dto.user.UserResponseDTO;
import com.example.airportManager.dto.user.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserResponseDTO> listAll(Pageable pageable);
    UserResponseDTO getById(UUID id);
    UserResponseDTO create(UserCreateDTO dto);
    UserResponseDTO update(UUID id, UserUpdateDTO dto);
    void delete(UUID id);
}
