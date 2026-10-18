package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);
    List<QuizAttempt> findByQuizIdOrderByScoreDescSubmittedAtAsc(Long quizId);
    List<QuizAttempt> findByStudentId(Long studentId);
    long countByQuizId(Long quizId);
    void deleteByQuizId(Long quizId);
}