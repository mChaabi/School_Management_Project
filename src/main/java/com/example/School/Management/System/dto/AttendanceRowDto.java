package com.example.School.Management.System.dto;

import com.example.School.Management.System.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRowDto {
    private Long studentId;
    private String studentName;
    private AttendanceStatus status = AttendanceStatus.PRESENT;  // default
    private String remark;
}