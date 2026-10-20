package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.enums.Role;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.mapper.StudentMapper;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository; // Optional: if you want to link classrooms
    private final StudentValidator validator;
    private final UserRepository userRepository;

    public StudentServiceImpl(StudentRepository studentRepository,
                              ClassroomRepository classroomRepository,
                              UserRepository userRepository,
                              StudentValidator validator) {
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Override
    public StudentDto createStudent(StudentDto studentDto) {
        validator.validate(studentDto, null);
        // Validate unique email
        if (studentRepository.existsByEmail(studentDto.email())) {
            throw new IllegalArgumentException("Email already exists: " + studentDto.email());
        }

        Classroom classroom = findClassroomById(studentDto.classroomId());
        Student student = StudentMapper.toEntity(studentDto, classroom);
        student.setParent(findParentById(studentDto.parentUserId()));

        Student savedStudent = studentRepository.save(student);
        return StudentMapper.toDto(savedStudent);
    }

    @Override
    public StudentDto updateStudent(Long id, StudentDto studentDto) {
        validator.validate(studentDto, id);
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Validate unique email excluding current student
        if (studentRepository.existsByEmailAndIdNot(studentDto.email(), id)) {
            throw new IllegalArgumentException("Email is already used by another student: " + studentDto.email());
        }

        Classroom classroom = findClassroomById(studentDto.classroomId());

        // Update fields
        existingStudent.setFirstName(studentDto.firstName());
        existingStudent.setLastName(studentDto.lastName());
        existingStudent.setEmail(studentDto.email());
        existingStudent.setPhone(studentDto.phone());
        existingStudent.setBirthDate(studentDto.birthDate());
        existingStudent.setGender(studentDto.gender());
        existingStudent.setClassroom(classroom);
        existingStudent.setCity(studentDto.city());
        existingStudent.setCountry(studentDto.country());
        existingStudent.setParent(findParentById(studentDto.parentUserId()));

        Student updatedStudent = studentRepository.save(existingStudent);
        return StudentMapper.toDto(updatedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return StudentMapper.toDto(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDto> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(StudentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDto> searchStudents(String keyword, Pageable pageable) {
        return studentRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                        keyword, keyword, keyword, keyword, pageable)
                .map(StudentMapper::toDto);
    }

    @Override
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private User findParentById(Long parentUserId) {
        if (parentUserId == null) {
            return null;
        }
        User user = userRepository.findById(parentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + parentUserId));
        if (!user.getRoles().contains(Role.ROLE_PARENT)) {
            throw new IllegalArgumentException("User is not a parent: " + user.getUsername());
        }
        return user;
    }

    // Helper method to fetch classroom safely if ID is present
    private Classroom findClassroomById(Long classroomId) {
        if (classroomId == null) {
            return null;
        }
        return classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + classroomId));
    }


}
