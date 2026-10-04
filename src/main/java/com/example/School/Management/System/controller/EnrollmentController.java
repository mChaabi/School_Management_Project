package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.EnrollmentDto;
import com.example.School.Management.System.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Mostrar lista de matrículas en una vista HTML (ej: enrollments/list.html)
    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("enrollments", enrollmentService.findAll());
        return "enrollments/list";
    }

    // Mostrar detalle de una matrícula (ej: enrollments/detail.html)
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {
        model.addAttribute("enrollment", enrollmentService.findById(id));
        return "enrollments/detail";
    }

    // Mostrar formulario de creación (ej: enrollments/form.html)
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("enrollmentDto", new EnrollmentDto(null, null, null, null, null));
        return "enrollments/form";
    }

    // Procesar la creación y redirigir a la lista
    @PostMapping
    public String create(@Valid @ModelAttribute("enrollmentDto") EnrollmentDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "enrollments/form";
        }
        enrollmentService.create(dto);
        return "redirect:/enrollments";
    }

    // Mostrar formulario de edición
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        EnrollmentDto dto = enrollmentService.findById(id);
        model.addAttribute("enrollmentDto", dto);
        return "enrollments/form";
    }

    // Procesar la actualización y redirigir
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("enrollmentDto") EnrollmentDto dto,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "enrollments/form";
        }
        enrollmentService.update(id, dto);
        return "redirect:/enrollments";
    }

    // Eliminar y redirigir a la lista
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return "redirect:/enrollments";
    }
}