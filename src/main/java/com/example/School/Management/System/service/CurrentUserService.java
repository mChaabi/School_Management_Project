package com.example.School.Management.System.service;

import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final StudentRepository studentRepo;

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean hasRole(String role) {
        return auth().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /** null = can see everything (admin/teacher) */
    public Set<Long> visibleStudentIds() {
        String username = auth().getName();
        if (hasRole("ADMIN") || hasRole("TEACHER")) return null;
        if (hasRole("STUDENT")) {
            return studentRepo.findByUser_Username(username)
                    .map(Student::getId)
                    .map(Set::of)
                    .orElse(Set.of());
        }
        if (hasRole("PARENT")) {
            // Return empty set until parent-student mapping is implemented
            return Set.of();
        }
        return Set.of();
    }

    public void assertCanSee(Long studentId) {
        Set<Long> ids = visibleStudentIds();
        if (ids != null && !ids.contains(studentId)) {
            throw new AccessDeniedException("Not your data");
        }
    }
}