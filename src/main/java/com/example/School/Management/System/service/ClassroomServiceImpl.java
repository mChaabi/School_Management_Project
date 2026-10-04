package com.example.School.Management.System.service;


import com.example.School.Management.System.dto.ClassroomDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.mapper.ClassroomMapper;
import com.example.School.Management.System.repository.ClassroomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomServiceImpl(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Override
    public ClassroomDto createClassroom(ClassroomDto classroomDto) {
        // Validate unique name
        if (classroomRepository.existsByName(classroomDto.name())) {
            throw new IllegalArgumentException("Classroom name already exists: " + classroomDto.name());
        }

        Classroom classroom = ClassroomMapper.toEntity(classroomDto);
        Classroom savedClassroom = classroomRepository.save(classroom);
        return ClassroomMapper.toDto(savedClassroom);
    }

    @Override
    public ClassroomDto updateClassroom(Long id, ClassroomDto classroomDto) {
        Classroom existingClassroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));

        // Validate unique name excluding current classroom
        if (classroomRepository.existsByNameAndIdNot(classroomDto.name(), id)) {
            throw new IllegalArgumentException("Classroom name is already used by another classroom: " + classroomDto.name());
        }

        // Update fields
        existingClassroom.setName(classroomDto.name());
        existingClassroom.setCapacity(classroomDto.capacity());

        Classroom updatedClassroom = classroomRepository.save(existingClassroom);
        return ClassroomMapper.toDto(updatedClassroom);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomDto getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + id));
        return ClassroomMapper.toDto(classroom);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassroomDto> getAllClassrooms(Pageable pageable) {
        return classroomRepository.findAll(pageable).map(ClassroomMapper::toDto);
    }

    @Override
    public void deleteClassroom(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom not found with id: " + id);
        }
        classroomRepository.deleteById(id);
    }
}
