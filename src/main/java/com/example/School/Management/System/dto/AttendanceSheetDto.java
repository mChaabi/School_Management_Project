package com.example.School.Management.System.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSheetDto {
    @NotNull(message = "Classroom is required")
    private Long classroomId;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date = LocalDate.now();

    @Valid
    private List<AttendanceRowDto> rows = new ArrayList<>();
}
