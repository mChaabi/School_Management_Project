package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, Long id);
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Course> findByTeacherIsNull();

    public interface CourseStats {
        Long getCourseId();
        String getTitle();
        Integer getCredits();
        Long getStudentCount();
        Double getAverage();
    }

    @Query("""
       select c.id as courseId, c.title as title, c.credits as credits,
              count(e) as studentCount, avg(e.grade) as average
       from Course c left join c.enrollments e
       where c.teacher.id = :teacherId
       group by c.id, c.title, c.credits
       order by c.title
       """)
    List<CourseStats> findStatsByTeacherId(@Param("teacherId") Long teacherId);
}
