package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseIdOrderByExamDateDesc(Long courseId);
    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);
}
