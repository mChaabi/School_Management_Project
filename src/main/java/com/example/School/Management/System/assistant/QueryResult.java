package com.example.School.Management.System.assistant;

import java.util.List;

public record QueryResult(List<String> columns, List<List<String>> rows, boolean truncated) {}