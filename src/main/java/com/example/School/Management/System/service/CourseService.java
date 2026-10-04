package com.example.School.Management.System.service;


import com.example.School.Management.System.dto.CourseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    CourseDto createCourse(CourseDto courseDto);
    CourseDto updateCourse(Long id, CourseDto courseDto);
    CourseDto getCourseById(Long id);
    Page<CourseDto> getAllCourses(Pageable pageable);
    Page<CourseDto> searchCourses(String keyword, Pageable pageable);
    void deleteCourse(Long id);
}