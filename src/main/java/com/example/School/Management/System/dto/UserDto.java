package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.Role;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        Set<Role> roles,
        boolean enabled
) {
}