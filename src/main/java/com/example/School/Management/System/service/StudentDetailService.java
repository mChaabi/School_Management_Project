package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.StudentCourseRow;
import com.example.School.Management.System.dto.StudentDetailDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.entity.Teacher;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentDetailService {

    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final CurrentUserService currentUser;   // from the security steps

    @Transactional(readOnly = true)
    public StudentDetailDto getDetail(Long id) {
        currentUser.assertCanSee(id);   // student/parent can only open their own

        Student s = studentRepo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Student not found: " + id));

        List<StudentCourseRow> rows = enrollmentRepo.findDetailsByStudentId(id).stream()
                .map(e -> {
                    Teacher t = e.getCourse().getTeacher();
                    return new StudentCourseRow(
                            e.getCourse().getId(),
                            e.getCourse().getTitle(),
                            e.getCourse().getCredits(),
                            t != null ? t.getFirstName() + " " + t.getLastName() : "Not assigned",
                            t != null ? t.getSpecialization() : null,
                            e.getEnrollmentDate(),
                            e.getGrade());
                })
                .toList();

        Double avg = rows.stream()
                .map(StudentCourseRow::grade)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream().boxed().findFirst().orElse(null);

        Classroom c = s.getClassroom();   // lazy, safe inside the transaction

        return new StudentDetailDto(
                s.getId(), s.getFirstName() + " " + s.getLastName(),
                s.getEmail(), s.getPhone(), s.getGender(), s.getBirthDate(),
                c != null ? c.getName() : null,
                c != null ? c.getCapacity() : null,
                rows, avg);
    }
}
