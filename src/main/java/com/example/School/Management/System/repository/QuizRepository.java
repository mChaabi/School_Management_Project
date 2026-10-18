package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseIdOrderByIdDesc(Long courseId);

    @Query("select q from Quiz q where q.published = true and q.course.id in " +
            "(select e.course.id from Enrollment e where e.student.id = :sid) order by q.id desc")
    List<Quiz> availableFor(@Param("sid") Long studentId);
}