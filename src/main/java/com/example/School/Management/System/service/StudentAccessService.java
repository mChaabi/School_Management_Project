package com.example.School.Management.System.service;

import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentAccessService {

    private final StudentRepository studentRepo;

    public boolean canView(Long studentId, Authentication auth) {
        if (auth == null || studentId == null) return false;

        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_TEACHER")) return true;

        if (roles.contains("ROLE_PARENT")
                && studentRepo.existsByIdAndParentUsername(studentId, auth.getName())) return true;

        return roles.contains("ROLE_STUDENT")
                && studentRepo.existsByIdAndUserUsername(studentId, auth.getName());
    }

    public void requireView(Long studentId, Authentication auth) {
        if (!canView(studentId, auth)) {
            throw new AccessDeniedException("Not allowed to view this student");
        }
    }

    public boolean canViewFinance(Long studentId, Authentication auth) {
        if (auth == null || studentId == null) return false;
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if (roles.contains("ROLE_ADMIN")) return true;
        if (roles.contains("ROLE_PARENT")
                && studentRepo.existsByIdAndParentUsername(studentId, auth.getName())) return true;
        return roles.contains("ROLE_STUDENT")
                && studentRepo.existsByIdAndUserUsername(studentId, auth.getName());
    }
}
