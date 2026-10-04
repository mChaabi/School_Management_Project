package com.example.School.Management.System.mapper;


import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.entity.Student;

public class StudentMapper {

    // Convert Entity to Record (DTO)
    public static StudentDto toDto(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhone(),
                student.getBirthDate(),
                student.getGender(),
                student.getClassroom() != null ? student.getClassroom().getId() : null
        );
    }

    // Convert Record (DTO) to Entity
    public static Student toEntity(StudentDto dto, Classroom classroom) {
        if (dto == null) {
            return null;
        }
        return Student.builder()
                .id(dto.id())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .phone(dto.phone())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .classroom(classroom)
                .build();
    }
}