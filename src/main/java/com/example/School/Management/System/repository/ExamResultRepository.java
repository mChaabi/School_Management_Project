package com.example.School.Management.System.repository;

import com.example.School.Management.System.dto.StudentExamResultDto;
import com.example.School.Management.System.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    List<ExamResult> findByExamId(Long examId);
    List<ExamResult> findByExamCourseId(Long courseId);
    long countByExamId(Long examId);

    @Query("""
        SELECT new com.example.School.Management.System.dto.StudentExamResultDto(
            er.id,
            c.title,
            CONCAT(t.firstName, ' ', t.lastName),
            CONCAT(s.firstName, ' ', s.lastName),
            e.title,
            er.score,
            e.maxScore,
            er.absent
        )
        FROM ExamResult er
        JOIN er.exam e
        JOIN e.course c
        LEFT JOIN c.teacher t
        JOIN er.student s
        WHERE c.id = :courseId
    """)
    List<StudentExamResultDto> findGradesByCourseId(@Param("courseId") Long courseId);
}