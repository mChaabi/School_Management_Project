package com.example.School.Management.System.service;
import com.example.School.Management.System.dto.EnrollmentDto;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Enrollment;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.mapper.EnrollmentMapper;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Transactional
    public EnrollmentDto create(EnrollmentDto dto) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(dto.studentId(), dto.courseId())) {
            throw new IllegalStateException("The student is already enrolled in this course");
        }

        Student student = studentRepository.findById(dto.studentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + dto.studentId()));
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + dto.courseId()));

        Enrollment saved = enrollmentRepository.save(enrollmentMapper.toEntity(dto, student, course));
        return enrollmentMapper.toDto(saved);
    }

    public List<EnrollmentDto> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public EnrollmentDto findById(Long id) {
        return enrollmentMapper.toDto(getEnrollmentOrThrow(id));
    }

    public List<EnrollmentDto> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public List<EnrollmentDto> findByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    @Transactional
    public EnrollmentDto update(Long id, EnrollmentDto dto) {
        Enrollment enrollment = getEnrollmentOrThrow(id);
        enrollmentMapper.updateEntity(enrollment, dto);
        return enrollmentMapper.toDto(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void delete(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    private Enrollment getEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));
    }
}