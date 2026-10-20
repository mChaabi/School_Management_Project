package com.example.School.Management.System.dto;


import java.util.List;

public record AccountInfo(Long userId, String username, String email,
                          boolean enabled, List<String> roles) {}
