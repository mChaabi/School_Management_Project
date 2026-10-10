package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
           select s from Schedule s
           where s.dayOfWeek = :day
             and s.startTime < :end and s.endTime > :start
             and (s.classroom.id = :classroomId
                  or (:teacherId is not null and s.course.teacher.id = :teacherId))
             and (:ignoreId is null or s.id <> :ignoreId)
           """)
    List<Schedule> findConflicts(@Param("day") DayOfWeek day,
                                 @Param("start") LocalTime start,
                                 @Param("end") LocalTime end,
                                 @Param("classroomId") Long classroomId,
                                 @Param("teacherId") Long teacherId,
                                 @Param("ignoreId") Long ignoreId);

    @Query("""
       select s from Schedule s
       join fetch s.course c
       left join fetch c.teacher
       join fetch s.classroom
       where (:classroomId is null or s.classroom.id = :classroomId)
         and (:teacherId is null or c.teacher.id = :teacherId)
       order by s.startTime
       """)
    List<Schedule> findForGrid(@Param("classroomId") Long classroomId,
                               @Param("teacherId") Long teacherId);
}