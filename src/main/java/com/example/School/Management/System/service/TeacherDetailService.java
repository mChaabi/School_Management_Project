package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.TeacherCourseRow;
import com.example.School.Management.System.dto.TeacherDetailDto;
import com.example.School.Management.System.entity.Teacher;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeacherDetailService {

    private final TeacherRepository teacherRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;

    @Transactional(readOnly = true)
    public TeacherDetailDto getDetail(Long id) {
        Teacher t = teacherRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + id));

        List<TeacherCourseRow> rows = courseRepo.findStatsByTeacherId(id).stream()
                .map(s -> new TeacherCourseRow(s.getCourseId(), s.getTitle(), s.getCredits(),
                        s.getStudentCount(), s.getAverage()))
                .toList();

        int credits = rows.stream()
                .map(TeacherCourseRow::credits).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).sum();

        Double overall = rows.stream()
                .map(TeacherCourseRow::average).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average()
                .stream().boxed().findFirst().orElse(null);

        return new TeacherDetailDto(
                t.getId(), t.getFirstName() + " " + t.getLastName(),
                t.getEmail(), t.getPhone(), t.getSpecialization(), t.getGender(),
                rows.size(), enrollmentRepo.countDistinctStudentsByTeacher(id),
                credits, overall, rows);
    }
}
