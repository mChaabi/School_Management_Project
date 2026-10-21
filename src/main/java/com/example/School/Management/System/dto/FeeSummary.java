package com.example.School.Management.System.dto;

import java.math.BigDecimal;

public record FeeSummary(BigDecimal pending, BigDecimal overdue, BigDecimal collected) {}
