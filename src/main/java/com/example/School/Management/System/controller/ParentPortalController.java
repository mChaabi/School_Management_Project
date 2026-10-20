package com.example.School.Management.System.controller;

import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.service.Student360Service;
import com.example.School.Management.System.service.StudentAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parent")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
public class ParentPortalController {

    private final StudentRepository studentRepo;
    private final Student360Service student360;
    private final StudentAccessService access;

    @GetMapping
    public String children(Authentication auth, Model model) {
        var children = studentRepo.findByParentUsername(auth.getName()).stream()
                .map(s -> student360.getProfile(s.getId()))
                .toList();
        model.addAttribute("children", children);
        return "parent/children";
    }

    @GetMapping("/children/{id}")
    public String child(@PathVariable Long id, Authentication auth, Model model) {
        access.requireView(id, auth);                       // 403 si no es su hijo
        model.addAttribute("p", student360.getProfile(id));
        return "parent/child";
    }
}