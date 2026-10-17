package com.example.School.Management.System.entity;

import com.lowagie.text.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExamSheetDto {
    private Long examId;
    private Long courseId;
    private String examTitle;
    private String courseTitle;
    private LocalDate examDate;
    private Double maxScore;
    private Integer weight;
    private List<Row> rows = new ArrayList<>();

    @Data @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private Long studentId;
        private String studentName;     // display only
        private Double score;           // null = not graded yet
        private boolean absent;
    }
}