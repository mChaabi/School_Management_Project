package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.FeeForm;
import com.example.School.Management.System.enums.FeeStatus;
import com.example.School.Management.System.enums.PaymentMethod;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/fees")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFeeController {

    private final FeeService feeService;
    private final StudentRepository studentRepo;
    private final ClassroomRepository classroomRepo;

    @GetMapping
    public String list(@RequestParam(required = false) FeeStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("fees", feeService.search(status, keyword, page));
        model.addAttribute("summary", feeService.summary());
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", FeeStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        return "admin/fees/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("feeForm", new FeeForm(null, null, "", null, LocalDate.now().plusDays(30)));
        addFormData(model);
        return "admin/fees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("feeForm") FeeForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                int created = feeService.create(form);
                if (created == 0) {
                    result.reject("fees.error.duplicates", "fees.error.duplicates");
                } else {
                    redirect.addFlashAttribute("created", created);
                    return "redirect:/admin/fees";
                }
            } catch (IllegalArgumentException | ResourceNotFoundException ex) {
                result.reject(ex.getMessage(), ex.getMessage());
            }
        }
        addFormData(model);
        return "admin/fees/form";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam PaymentMethod method, RedirectAttributes redirect) {
        try {
            feeService.markPaid(id, method);
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fees";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            feeService.cancel(id);
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fees";
    }

    private void addFormData(Model model) {
        model.addAttribute("students", studentRepo.findAll(Sort.by("lastName", "firstName")));
        model.addAttribute("classrooms", classroomRepo.findAll(Sort.by("name")));
    }
}
