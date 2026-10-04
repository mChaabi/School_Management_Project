package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.CourseDto;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Teacher;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.mapper.CourseMapper;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public CourseServiceImpl(CourseRepository courseRepository, TeacherRepository teacherRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public CourseDto createCourse(CourseDto courseDto) {
        if (courseRepository.existsByTitle(courseDto.title())) {
            throw new IllegalArgumentException("Course title already exists: " + courseDto.title());
        }

        Teacher teacher = findTeacherById(courseDto.teacherId());
        Course course = CourseMapper.toEntity(courseDto, teacher);

        Course savedCourse = courseRepository.save(course);
        return CourseMapper.toDto(savedCourse);
    }

    @Override
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (courseRepository.existsByTitleAndIdNot(courseDto.title(), id)) {
            throw new IllegalArgumentException("Course title is already used: " + courseDto.title());
        }

        Teacher teacher = findTeacherById(courseDto.teacherId());

        existingCourse.setTitle(courseDto.title());
        existingCourse.setDescription(courseDto.description());
        existingCourse.setCredits(courseDto.credits());
        existingCourse.setTeacher(teacher);

        Course updatedCourse = courseRepository.save(existingCourse);
        return CourseMapper.toDto(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return CourseMapper.toDto(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(CourseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword, pageable).map(CourseMapper::toDto);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    private Teacher findTeacherById(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
    }
}
