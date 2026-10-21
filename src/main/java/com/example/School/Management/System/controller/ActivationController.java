package com.example.School.Management.System.controller;

import com.example.School.Management.System.service.AccountActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ActivationController {

    private final AccountActivationService service;

    @GetMapping("/activate")
    public String activate(@RequestParam String token) {
        return service.activate(token) ? "redirect:/login?activated" : "redirect:/login?invalidToken";
    }

    @GetMapping("/activation/resend")
    public String resendForm() {
        return "auth/resend-activation";
    }

    @PostMapping("/activation/resend")
    public String resend(@RequestParam String email, Model model) {
        service.resend(email);
        model.addAttribute("sent", true);      // mismo mensaje exista o no el email
        return "auth/resend-activation";
    }
}
