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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CurrentUserService currentUser;

    // Inject your current user security helper component
    // private final CurrentUserService currentUser;

    /**
     * Filters enrollments based on the currently logged-in user's role and visibility rules.
     */
    public List<EnrollmentDto> findForCurrentUser(Authentication auth) {
        Set<Long> ids = currentUser.visibleStudentIds();

        List<Enrollment> list = (ids == null)
                ? enrollmentRepository.findAll()
                : enrollmentRepository.findByStudentIdIn(ids.stream().toList()); // Converted Set to List here

        return list.stream().map(enrollmentMapper::toDto).toList();
    }

    @Transactional
    public EnrollmentDto create(EnrollmentDto dto) {
        // Optional: Ensure user has permission to create enrollment for this student
        // currentUser.assertCanSee(dto.studentId());

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
        EnrollmentDto dto = enrollmentMapper.toDto(getEnrollmentOrThrow(id));

        // Assert that the current user is authorized to view this student's enrollment
        currentUser.assertCanSee(dto.studentId()); // Throws 403 / AccessDeniedException if not allowed

        return dto;
    }

    public List<EnrollmentDto> findByStudentId(Long studentId) {
        // Assert visibility for the requested student ID
        currentUser.assertCanSee(studentId);

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

        // Ensure user can see/modify the existing enrollment's student
        currentUser.assertCanSee(enrollment.getStudent().getId());

        enrollmentMapper.updateEntity(enrollment, dto);
        return enrollmentMapper.toDto(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = getEnrollmentOrThrow(id);

        // Ensure user has permission over this student's record before deleting
        currentUser.assertCanSee(enrollment.getStudent().getId());

        enrollmentRepository.deleteById(id);
    }

    private Enrollment getEnrollmentOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + id));
    }
}