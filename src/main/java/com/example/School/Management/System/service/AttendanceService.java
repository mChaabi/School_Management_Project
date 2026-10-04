package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.AttendanceDto;
import com.example.School.Management.System.dto.AttendanceSheetDto;
import com.example.School.Management.System.dto.AttendanceSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceSheetDto getSheet(Long classroomId, LocalDate date);

    AttendanceSheetDto saveSheet(AttendanceSheetDto sheet);

    List<AttendanceDto> findByStudentId(Long studentId);

    AttendanceSummaryDto getSummary(Long studentId);

    AttendanceDto findById(Long id);

    AttendanceDto update(Long id, AttendanceDto dto);

    void delete(Long id);
}