package com.example.School.Management.System.repository;

import com.example.School.Management.System.dto.DayStatusCount;
import com.example.School.Management.System.dto.StudentAbsence;
import com.example.School.Management.System.entity.Attendance;
import com.example.School.Management.System.enums.AttendanceStatus;
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

    List<Attendance> findByStudentIdIn(List<Long> studentIds);

    @Query("""
       select a.date as label, a.status as status, count(a) as total
       from Attendance a
       where a.date >= :from
       group by a.date, a.status
       order by a.date
       """)
    List<DayStatusCount> countByDayAndStatus(@Param("from") LocalDate from);

    @Query("select count(a) from Attendance a where a.date = :day")
    long countByDay(@Param("day") LocalDate day);

    @Query("select count(a) from Attendance a where a.date = :day and a.status = :status")
    long countByDayAndStatus(@Param("day") LocalDate day, @Param("status") AttendanceStatus status);

    @Query("""
       select a.student.id as studentId, count(a) as total
       from Attendance a
       where a.status = com.example.School.Management.System.enums.AttendanceStatus.ABSENT
         and a.date >= :from
       group by a.student.id
       having count(a) >= :min
       """)
    List<StudentAbsence> findFrequentAbsentees(@Param("from") LocalDate from, @Param("min") long min);
}