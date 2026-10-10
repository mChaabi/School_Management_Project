package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.AttendanceStatus;

import java.time.LocalDate;

public interface DayStatusCount { LocalDate getLabel(); AttendanceStatus getStatus(); Long getTotal(); }
