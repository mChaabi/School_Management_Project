package com.example.School.Management.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/help")
    public String help() {
        return "footer/help";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "footer/privacy";
    }

    @GetMapping("/contact")
    public String contact() {
        return "footer/contact";
    }
}