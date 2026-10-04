package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.ClassroomDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassroomService {

    ClassroomDto createClassroom(ClassroomDto classroomDto);

    ClassroomDto updateClassroom(Long id, ClassroomDto classroomDto);

    ClassroomDto getClassroomById(Long id);

    Page<ClassroomDto> getAllClassrooms(Pageable pageable);

    void deleteClassroom(Long id);
}
