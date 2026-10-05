package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.CourseDto;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Teacher;

public class CourseMapper {

    // Convert Course Entity to CourseDto
    public static CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }

        return new CourseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCredits(),
                course.getTeacher() != null ? course.getTeacher().getId() : null,
                course.getTeacher() != null
                        ? course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName()
                        : null
        );
    }

    // Convert CourseDto to Course Entity
    public static Course toEntity(CourseDto dto, Teacher teacher) {
        if (dto == null) {
            return null;
        }

        return Course.builder()
                .id(dto.id())
                .title(dto.title())
                .description(dto.description())
                .credits(dto.credits())
                .teacher(teacher)
                .build();
    }
}
