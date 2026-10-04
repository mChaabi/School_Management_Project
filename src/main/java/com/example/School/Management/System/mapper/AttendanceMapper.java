package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.AttendanceDto;
import com.example.School.Management.System.dto.AttendanceRowDto;
import com.example.School.Management.System.dto.AttendanceSheetDto;
import com.example.School.Management.System.dto.AttendanceSummaryDto;
import com.example.School.Management.System.entity.Attendance;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.enums.AttendanceStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AttendanceMapper {

    // ---------------------------------------------------------------
    // AttendanceDto  <->  Attendance
    // ---------------------------------------------------------------

    public AttendanceDto toDto(Attendance attendance) {
        if (attendance == null) return null;
        return new AttendanceDto(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getRemark()
        );
    }

    public Attendance toEntity(AttendanceDto dto, Student student) {
        if (dto == null) return null;
        return Attendance.builder()
                .student(student)
                .date(dto.date())
                .status(dto.status())
                .remark(dto.remark())
                .build();
    }

    /** Only status and remark can change (student + date are the unique key). */
    public void updateEntity(Attendance attendance, AttendanceDto dto) {
        attendance.setStatus(dto.status());
        attendance.setRemark(dto.remark());
    }

    // ---------------------------------------------------------------
    // AttendanceRowDto  <->  Attendance
    // ---------------------------------------------------------------

    /**
     * Builds one row of the sheet for a student.
     * If the student has no attendance yet (attendance == null),
     * the row keeps the DTO default status (PRESENT) and an empty remark.
     */
    public AttendanceRowDto toRowDto(Student student, Attendance attendance) {
        AttendanceRowDto row = new AttendanceRowDto();
        row.setStudentId(student.getId());
        row.setStudentName(fullName(student));
        if (attendance != null) {
            row.setStatus(attendance.getStatus());
            row.setRemark(attendance.getRemark());
        }
        return row;
    }

    /**
     * Builds all rows for a classroom: one per student, filled with the
     * existing attendance of that day (key = studentId) when there is one.
     */
    public List<AttendanceRowDto> toRowDtos(List<Student> students,
                                            Map<Long, Attendance> existingByStudentId) {
        return students.stream()
                .map(s -> toRowDto(s, existingByStudentId.get(s.getId())))
                .toList();
    }

    public Attendance toEntity(AttendanceRowDto row, LocalDate date, Student student) {
        if (row == null) return null;
        return Attendance.builder()
                .student(student)
                .date(date)
                .status(row.getStatus())
                .remark(row.getRemark())
                .build();
    }

    public void updateEntity(Attendance attendance, AttendanceRowDto row) {
        attendance.setStatus(row.getStatus());
        attendance.setRemark(row.getRemark());
    }

    // ---------------------------------------------------------------
    // AttendanceSheetDto  <->  List<Attendance>
    // ---------------------------------------------------------------

    public AttendanceSheetDto toSheetDto(Long classroomId, LocalDate date, List<AttendanceRowDto> rows) {
        AttendanceSheetDto sheet = new AttendanceSheetDto();
        sheet.setClassroomId(classroomId);
        sheet.setDate(date);
        sheet.setRows(rows);
        return sheet;
    }

    /**
     * Converts every row of the sheet into a new Attendance entity.
     * The service loads the students first and passes them as a map (id -> Student).
     */
    public List<Attendance> toEntities(AttendanceSheetDto sheet, Map<Long, Student> studentsById) {
        return sheet.getRows().stream()
                .map(row -> {
                    Student student = studentsById.get(row.getStudentId());
                    if (student == null) {
                        throw new EntityNotFoundException("Student not found with id: " + row.getStudentId());
                    }
                    return toEntity(row, sheet.getDate(), student);
                })
                .toList();
    }

    // ---------------------------------------------------------------
    // AttendanceSummaryDto
    // ---------------------------------------------------------------

    public AttendanceSummaryDto toSummary(Collection<Attendance> attendances) {
        Map<AttendanceStatus, Long> counts = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));

        return new AttendanceSummaryDto(
                counts.getOrDefault(AttendanceStatus.PRESENT, 0L),
                counts.getOrDefault(AttendanceStatus.ABSENT, 0L),
                counts.getOrDefault(AttendanceStatus.LATE, 0L),
                counts.getOrDefault(AttendanceStatus.EXCUSED, 0L)
        );
    }

    // ---------------------------------------------------------------

    /** Adjust to your Student entity fields (e.g. getName()). */
    private String fullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }
}