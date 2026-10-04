package com.example.School.Management.System.mapper;

import com.example.School.Management.System.dto.ClassroomDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassroomMapper {

    public static ClassroomDto toDto(Classroom classroom) {
        if (classroom == null) {
            return null;
        }

        List<Long> studentIds = classroom.getStudents() != null
                ? classroom.getStudents().stream().map(Student::getId).toList()
                : new ArrayList<>();

        return new ClassroomDto(
                classroom.getId(),
                classroom.getName(),
                classroom.getCapacity(),
                studentIds
        );
    }

    public static Classroom toEntity(ClassroomDto dto) {
        if (dto == null) {
            return null;
        }
        return Classroom.builder()
                .id(dto.id())
                .name(dto.name())
                .capacity(dto.capacity())
                // La lista de estudiantes se suele gestionar desde el lado del Student o mediante el servicio
                .students(new ArrayList<>())
                .build();
    }
}
