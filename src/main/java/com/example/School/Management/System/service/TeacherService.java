package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.TeacherDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherService {
    TeacherDto createTeacher(TeacherDto teacherDto);
    TeacherDto updateTeacher(Long id, TeacherDto teacherDto);
    TeacherDto getTeacherById(Long id);
    Page<TeacherDto> getAllTeachers(Pageable pageable);
    Page<TeacherDto> searchTeachers(String keyword, Pageable pageable);
    void deleteTeacher(Long id);
}
