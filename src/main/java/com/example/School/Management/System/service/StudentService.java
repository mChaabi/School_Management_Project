package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.StudentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentDto createStudent(StudentDto studentDto);

    StudentDto updateStudent(Long id, StudentDto studentDto);

    StudentDto getStudentById(Long id);

    Page<StudentDto> getAllStudents(Pageable pageable);

    Page<StudentDto> searchStudents(String keyword, Pageable pageable);

    void deleteStudent(Long id);
}
