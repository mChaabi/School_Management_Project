package com.example.School.Management.System.dto;

public record ImportRow(int rowNumber, String firstName, String lastName, String email,
                        String phone, String city, String country,
                        String birthDate, String gender, String classroom) {}
