package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Exam;
import com.example.School.Management.System.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseIdOrderByExamDateDesc(Long courseId);
    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);
    @Query("""
        select e from Exam e
          join fetch e.course c
          left join fetch c.teacher t
          left join t.user u
        where e.examDate between :from and :to
          and (:username is null or u.username = :username)
        order by e.examDate, c.title, e.title
        """)
    List<Exam> findBetween(@Param("from") LocalDate from,
                           @Param("to") LocalDate to,
                           @Param("username") String username);
}
