package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.UserDto;
import com.example.School.Management.System.dto.UserRegistrationDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto registerUser(UserRegistrationDto registrationDto);
    Optional<UserDto> findByUsername(String username);
    List<UserDto> getAllUsers();

    // Nuevos métodos para actualizar y eliminar
    UserDto updateUser(Long id, UserRegistrationDto updateDto);
    void deleteUser(Long id);
}