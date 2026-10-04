package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.TeacherDto;
import com.example.School.Management.System.entity.Teacher;

public class TeacherMapper {

    // Convert Teacher Entity to TeacherDto Record
    public static TeacherDto toDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return new TeacherDto(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getEmail(),
                teacher.getPhone(),
                teacher.getSpecialization(),
                teacher.getGender()
        );
    }

    // Convert TeacherDto Record to Teacher Entity
    public static Teacher toEntity(TeacherDto dto) {
        if (dto == null) {
            return null;
        }

        return Teacher.builder()
                .id(dto.id())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .phone(dto.phone())
                .specialization(dto.specialization())
                .gender(dto.gender())
                .build();
    }
}
