package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.TeacherDto;
import com.example.School.Management.System.entity.Teacher;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.mapper.TeacherMapper;
import com.example.School.Management.System.repository.TeacherRepository;
import com.example.School.Management.System.service.TeacherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public TeacherDto createTeacher(TeacherDto teacherDto) {
        if (teacherRepository.existsByEmail(teacherDto.email())) {
            throw new IllegalArgumentException("Email already exists: " + teacherDto.email());
        }

        Teacher teacher = TeacherMapper.toEntity(teacherDto);
        Teacher savedTeacher = teacherRepository.save(teacher);
        return TeacherMapper.toDto(savedTeacher);
    }

    @Override
    public TeacherDto updateTeacher(Long id, TeacherDto teacherDto) {
        Teacher existingTeacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (teacherRepository.existsByEmailAndIdNot(teacherDto.email(), id)) {
            throw new IllegalArgumentException("Email is already used by another teacher: " + teacherDto.email());
        }

        existingTeacher.setFirstName(teacherDto.firstName());
        existingTeacher.setLastName(teacherDto.lastName());
        existingTeacher.setEmail(teacherDto.email());
        existingTeacher.setPhone(teacherDto.phone());
        existingTeacher.setSpecialization(teacherDto.specialization());
        existingTeacher.setGender(teacherDto.gender());

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);
        return TeacherMapper.toDto(updatedTeacher);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return TeacherMapper.toDto(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherDto> getAllTeachers(Pageable pageable) {
        return teacherRepository.findAll(pageable).map(TeacherMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherDto> searchTeachers(String keyword, Pageable pageable) {
        return teacherRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword, pageable)
                .map(TeacherMapper::toDto);
    }

    @Override
    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }
}
