package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.ScheduleCell;
import com.example.School.Management.System.dto.ScheduleDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Schedule;
import com.example.School.Management.System.entity.Teacher;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository repo;
    private final CourseRepository courseRepo;
    private final ClassroomRepository classroomRepo;

    @Transactional
    public Schedule save(Long id, Long courseId, Long classroomId,
                         DayOfWeek day, LocalTime start, LocalTime end) {

        if (!start.isBefore(end))
            throw new IllegalArgumentException("End time must be after start time.");

        Course course = courseRepo.findById(courseId).orElseThrow();
        Classroom room = classroomRepo.findById(classroomId).orElseThrow();
        Long teacherId = course.getTeacher() != null ? course.getTeacher().getId() : null;

        List<Schedule> conflicts = repo.findConflicts(day, start, end, classroomId, teacherId, id);
        if (!conflicts.isEmpty()) {
            Schedule c = conflicts.get(0);
            boolean sameRoom = c.getClassroom().getId().equals(classroomId);
            throw new IllegalStateException(sameRoom
                    ? "Room " + room.getName() + " is already used by " + c.getCourse().getTitle()
                    + " (" + c.getStartTime() + "-" + c.getEndTime() + ")."
                    : "The teacher already teaches " + c.getCourse().getTitle()
                    + " at that time in " + c.getClassroom().getName() + ".");
        }

        Schedule s = id == null ? new Schedule() : repo.findById(id).orElseThrow();
        s.setCourse(course);
        s.setClassroom(room);
        s.setDayOfWeek(day);
        s.setStartTime(start);
        s.setEndTime(end);
        return repo.save(s);
    }

    @Transactional(readOnly = true)
    public Map<DayOfWeek, List<ScheduleCell>> grid(Long classroomId, Long teacherId) {
        Map<DayOfWeek, List<ScheduleCell>> map = new EnumMap<>(DayOfWeek.class);
        for (Schedule s : repo.findForGrid(classroomId, teacherId)) {
            Teacher t = s.getCourse().getTeacher();
            map.computeIfAbsent(s.getDayOfWeek(), d -> new ArrayList<>())
                    .add(new ScheduleCell(
                            s.getId(),
                            s.getCourse().getTitle(),
                            s.getClassroom().getName(),
                            t != null ? t.getFirstName() + " " + t.getLastName() : "No teacher",
                            s.getStartTime(), s.getEndTime()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public ScheduleDto findDto(Long id) {
        Schedule s = repo.findById(id).orElseThrow();
        ScheduleDto dto = new ScheduleDto();
        dto.setId(s.getId());
        dto.setCourseId(s.getCourse().getId());
        dto.setClassroomId(s.getClassroom().getId());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
