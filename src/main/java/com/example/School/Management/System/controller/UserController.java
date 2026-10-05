package com.example.School.Management.System.controller;

import com.example.School.Management.System.dto.UserDto;
import com.example.School.Management.System.dto.UserRegistrationDto;
import com.example.School.Management.System.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Listar todos los usuarios
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "user/list"; // Ruta de tu template Thymeleaf para listar
    }

    // Mostrar formulario de registro
    @GetMapping("/new")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto("", "", "", null));
        return "user/form"; // Ruta de tu template Thymeleaf de formulario
    }

    // Procesar el registro / creación de usuario
    @PostMapping
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/form";
        }
        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("message", "User registered successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "user/form";
        }
        return "redirect:/users";
    }

    // Mostrar formulario de edición
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.getAllUsers().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .map(user -> {
                    model.addAttribute("userId", id);
                    model.addAttribute("userDto", new UserRegistrationDto(user.username(), "", user.email(), user.roles()));
                    return "user/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "User not found");
                    return "redirect:/users";
                });
    }

    // Procesar actualización de usuario
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userDto") UserRegistrationDto updateDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/form";
        }
        try {
            userService.updateUser(id, updateDto);
            redirectAttributes.addFlashAttribute("message", "User updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    // Eliminar usuario
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }
}