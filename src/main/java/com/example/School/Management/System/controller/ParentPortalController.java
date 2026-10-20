package com.example.School.Management.System.controller;

import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.service.Student360Service;
import com.example.School.Management.System.service.StudentAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/parent")
@PreAuthorize("hasAnyRole('PARENT', 'ADMIN', 'TEACHER')")
@RequiredArgsConstructor
public class ParentPortalController {

    private final StudentRepository studentRepo;
    private final Student360Service student360;
    private final StudentAccessService access;

    @GetMapping
    public String children(@RequestParam(required = false) String keyword, Authentication auth, Model model) {
        boolean isAdminOrTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

        java.util.List<com.example.School.Management.System.dto.Student360Dto> childrenProfiles;

        if (isAdminOrTeacher) {
            // El admin/profesor puede buscar o ver todos los estudiantes en formato 360
            var students = (keyword != null && !keyword.isBlank())
                    ? studentRepo.searchAll(keyword.trim(), Pageable.unpaged()).getContent()
                    : studentRepo.findAll();

            childrenProfiles = students.stream()
                    .map(s -> student360.getProfile(s.getId()))
                    .toList();

            model.addAttribute("isAdminView", true);
            model.addAttribute("keyword", keyword);
        } else {
            // El padre ve exclusivamente a sus hijos
            childrenProfiles = studentRepo.findByParentUsername(auth.getName()).stream()
                    .map(s -> student360.getProfile(s.getId()))
                    .toList();
            model.addAttribute("isAdminView", false);
        }

        model.addAttribute("children", childrenProfiles);
        model.addAttribute("currentMenu", "parent");
        return "parent/children";
    }

    @GetMapping("/children/{id}")
    public String child(@PathVariable Long id, Authentication auth, Model model) {
        boolean isAdminOrTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));

        // Si no es admin/profesor, validamos que sea su hijo de forma estricta
        if (!isAdminOrTeacher) {
            access.requireView(id, auth);
        }

        model.addAttribute("p", student360.getProfile(id));
        model.addAttribute("currentMenu", "parent");
        return "parent/child";
    }
}