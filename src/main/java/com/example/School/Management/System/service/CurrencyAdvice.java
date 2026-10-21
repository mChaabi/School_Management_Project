package com.example.School.Management.System.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrencyAdvice {
    @Value("${school.currency:MAD}")
    private String currency;

    @ModelAttribute("currency")
    public String currency() { return currency; }
}