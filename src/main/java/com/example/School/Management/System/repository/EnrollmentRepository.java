package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    // Add this to find enrollments for a list of student IDs
    List<Enrollment> findByStudentIdIn(List<Long> studentIds);
}
