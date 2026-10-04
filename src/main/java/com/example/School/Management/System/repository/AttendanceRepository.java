package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentClassroomIdAndDate(Long classroomId, LocalDate date);

    List<Attendance> findByStudentIdOrderByDateDesc(Long studentId);

    @Query("""
           select a.status, count(a) from Attendance a
           where a.student.id = :studentId
           group by a.status
           """)
    List<Object[]> countByStatus(@Param("studentId") Long studentId);
}
