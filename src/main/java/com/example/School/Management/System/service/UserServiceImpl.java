package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.UserDto;
import com.example.School.Management.System.dto.UserRegistrationDto;
import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.mapper.UserMapper;
import com.example.School.Management.System.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.username()).isPresent()) {
            throw new RuntimeException("Username is already taken: " + registrationDto.username());
        }

        User user = new User();
        user.setUsername(registrationDto.username());
        user.setEmail(registrationDto.email());
        user.setPassword(passwordEncoder.encode(registrationDto.password()));
        user.setRoles(registrationDto.roles());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserRegistrationDto updateDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setUsername(updateDto.username());
        existingUser.setEmail(updateDto.email());
        existingUser.setRoles(updateDto.roles());

        // Actualizar contraseña solo si se proporciona una nueva
        if (updateDto.password() != null && !updateDto.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updateDto.password()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}