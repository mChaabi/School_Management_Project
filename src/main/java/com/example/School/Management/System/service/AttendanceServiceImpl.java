package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.AttendanceDto;
import com.example.School.Management.System.dto.AttendanceRowDto;
import com.example.School.Management.System.dto.AttendanceSheetDto;
import com.example.School.Management.System.dto.AttendanceSummaryDto;
import com.example.School.Management.System.entity.Attendance;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.mapper.AttendanceMapper;
import com.example.School.Management.System.repository.AttendanceRepository;
import com.example.School.Management.System.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final AttendanceMapper attendanceMapper;

    /**
     * Filters attendance records based on the currently logged-in user's role.
     */
    public List<AttendanceDto> findForCurrentUser(Authentication auth) {
        if (hasRole(auth, "ADMIN") || hasRole(auth, "TEACHER")) {
            return attendanceRepository.findAll().stream()
                    .map(attendanceMapper::toDto)
                    .toList();
        }
        if (hasRole(auth, "STUDENT")) {
            Student student = studentRepository.findByUserUsername(auth.getName())
                    .orElseThrow(() -> new EntityNotFoundException("Student profile not found for user: " + auth.getName()));
            return findByStudentId(student.getId());
        }
        return List.of();
    }
    /** Rows for every student of the classroom, filled with the attendance already saved that day. */
    @Override
    public AttendanceSheetDto getSheet(Long classroomId, LocalDate date) {
        List<Student> students = studentRepository.findByClassroomId(classroomId);
        Map<Long, Attendance> existing = existingByStudent(classroomId, date);
        return attendanceMapper.toSheetDto(classroomId, date,
                attendanceMapper.toRowDtos(students, existing));
    }

    /** Upsert: updates the rows that already exist for that day, creates the rest. */
    @Override
    @Transactional
    public AttendanceSheetDto saveSheet(AttendanceSheetDto sheet) {
        Map<Long, Attendance> existing = existingByStudent(sheet.getClassroomId(), sheet.getDate());

        List<Long> studentIds = sheet.getRows().stream().map(AttendanceRowDto::getStudentId).toList();
        Map<Long, Student> students = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        List<Attendance> toSave = new ArrayList<>();
        for (AttendanceRowDto row : sheet.getRows()) {
            Attendance current = existing.get(row.getStudentId());
            if (current != null) {
                attendanceMapper.updateEntity(current, row);
                toSave.add(current);
            } else {
                Student student = students.get(row.getStudentId());
                if (student == null) {
                    throw new EntityNotFoundException("Student not found with id: " + row.getStudentId());
                }
                toSave.add(attendanceMapper.toEntity(row, sheet.getDate(), student));
            }
        }
        attendanceRepository.saveAll(toSave);
        return getSheet(sheet.getClassroomId(), sheet.getDate());
    }

    @Override
    public List<AttendanceDto> findByStudentId(Long studentId) {
        return attendanceRepository.findByStudentIdOrderByDateDesc(studentId).stream()
                .map(attendanceMapper::toDto)
                .toList();
    }

    @Override
    public AttendanceSummaryDto getSummary(Long studentId) {
        Map<AttendanceStatus, Long> counts = new EnumMap<>(AttendanceStatus.class);
        for (Object[] row : attendanceRepository.countByStatus(studentId)) {
            counts.put((AttendanceStatus) row[0], (Long) row[1]);
        }
        return new AttendanceSummaryDto(
                counts.getOrDefault(AttendanceStatus.PRESENT, 0L),
                counts.getOrDefault(AttendanceStatus.ABSENT, 0L),
                counts.getOrDefault(AttendanceStatus.LATE, 0L),
                counts.getOrDefault(AttendanceStatus.EXCUSED, 0L));
    }

    @Override
    public AttendanceDto findById(Long id) {
        return attendanceMapper.toDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public AttendanceDto update(Long id, AttendanceDto dto) {
        Attendance attendance = getOrThrow(id);
        attendanceMapper.updateEntity(attendance, dto);
        return attendanceMapper.toDto(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Attendance not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    private Map<Long, Attendance> existingByStudent(Long classroomId, LocalDate date) {
        return attendanceRepository.findByStudentClassroomIdAndDate(classroomId, date).stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity()));
    }

    private Attendance getOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));
    }

    private boolean hasRole(Authentication auth, String role) {
        String targetRole = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(targetRole));
    }
}