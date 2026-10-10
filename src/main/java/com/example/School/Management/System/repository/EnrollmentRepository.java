package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    // Add this to find enrollments for a list of student IDs
    List<Enrollment> findByStudentIdIn(List<Long> studentIds);

    @Query("""
           select e from Enrollment e
           join fetch e.course c
           left join fetch c.teacher
           where e.student.id = :studentId
           order by c.title
           """)
    List<Enrollment> findDetailsByStudentId(@Param("studentId") Long studentId);

    @Query("select count(distinct e.student.id) from Enrollment e where e.course.teacher.id = :teacherId")
    long countDistinctStudentsByTeacher(@Param("teacherId") Long teacherId);
}
