package com.example.School.Management.System.assistant;

import java.util.List;

public record AssistantAnswer(String answer, String sql, List<String> columns,
                              List<List<String>> rows, boolean truncated) {
    public static AssistantAnswer message(String text) {
        return new AssistantAnswer(text, null, List.of(), List.of(), false);
    }
}
