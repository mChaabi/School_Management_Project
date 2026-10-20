package com.example.School.Management.System.dto;

import java.util.List;

public record ImportResult(int total, int created, List<RowError> errors) {}
