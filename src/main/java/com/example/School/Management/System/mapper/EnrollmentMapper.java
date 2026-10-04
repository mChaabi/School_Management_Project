package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.EnrollmentDto;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Enrollment;
import com.example.School.Management.System.entity.Student;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EnrollmentMapper {

    public EnrollmentDto toDto(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        return new EnrollmentDto(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getCourse().getId(),
                enrollment.getEnrollmentDate(),
                enrollment.getGrade()
        );
    }

    /** Student y Course se resuelven en el service, porque el DTO solo trae los IDs. */
    public Enrollment toEntity(EnrollmentDto dto, Student student, Course course) {
        if (dto == null) {
            return null;
        }
        return Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(dto.enrollmentDate() != null ? dto.enrollmentDate() : LocalDate.now())
                .grade(dto.grade())
                .build();
    }

    /** Actualiza solo los campos modificables (fecha y nota). */
    public void updateEntity(Enrollment enrollment, EnrollmentDto dto) {
        if (dto.enrollmentDate() != null) {
            enrollment.setEnrollmentDate(dto.enrollmentDate());
        }
        enrollment.setGrade(dto.grade());
    }
}