package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.UserDto;
import com.example.School.Management.System.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled()
        );
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.id());          // En los records, los getters son directos (ej: dto.id())
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setRoles(dto.roles());
        user.setEnabled(dto.enabled());
        return user;
    }
}