package com.example.School.Management.System.config;

import com.example.School.Management.System.dto.UserRegistrationDto;
import com.example.School.Management.System.enums.Role;
import com.example.School.Management.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        List<UserRegistrationDto> users = List.of(
                new UserRegistrationDto("admin_mohamed",   "SecurePassword123!",  "mohamed.admin@school.com",  new HashSet<>(Set.of(Role.ROLE_ADMIN))),
                new UserRegistrationDto("teacher_sarah",   "TeacherPassword456@", "sarah.teacher@school.com",  new HashSet<>(Set.of(Role.ROLE_TEACHER))),
                new UserRegistrationDto("student_youssef", "StudentPassword789#", "youssef.student@school.com", new HashSet<>(Set.of(Role.ROLE_STUDENT))),
                new UserRegistrationDto("parent_fatima",   "ParentPassword321$",  "fatima.parent@school.com",  new HashSet<>(Set.of(Role.ROLE_PARENT)))
        );

        for (UserRegistrationDto dto : users) {
            userService.findByUsername(dto.username()).ifPresentOrElse(
                    existing -> userService.updateUser(existing.id(), dto),  // resets the password hash
                    () -> userService.registerUser(dto)
            );
        }
        System.out.println(">>> Default users are ready");
    }
}