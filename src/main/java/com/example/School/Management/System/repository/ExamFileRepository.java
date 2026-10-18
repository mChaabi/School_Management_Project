package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.ExamFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExamFileRepository extends JpaRepository<ExamFile, Long> {

    Optional<ExamFile> findByExamId(Long examId);                       // with the content

    <T> Optional<T> findByExamId(Long examId, Class<T> type);           // ExamFileInfo, without the content

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ExamFile f where f.exam.id = :examId")
    void deleteByExamId(@Param("examId") Long examId);
}
