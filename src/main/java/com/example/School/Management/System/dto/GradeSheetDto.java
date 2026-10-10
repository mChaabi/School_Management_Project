package com.example.School.Management.System.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GradeSheetDto {
    private Long courseId;
    private String courseTitle;
    private List<GradeRow> rows = new ArrayList<>();
}