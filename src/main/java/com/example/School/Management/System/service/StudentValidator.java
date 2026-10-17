package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.entity.Classroom;
import com.example.School.Management.System.exception.FieldValidationException;
import com.example.School.Management.System.repository.ClassroomRepository;
import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class StudentValidator {

    private static final int MIN_AGE = 5;    // use 16 or 18 for an adult-only school
    private static final int MAX_AGE = 100;

    private final StudentRepository students;
    private final ClassroomRepository classrooms;

    /** currentId = null when creating, the student's id when updating. */
    @Transactional(readOnly = true)
    public void validate(StudentDto d, Long currentId) {
        boolean creating = currentId == null;

        // 1. Email unique
        boolean emailTaken = creating
                ? students.existsByEmailIgnoreCase(d.email())
                : students.existsByEmailIgnoreCaseAndIdNot(d.email(), currentId);
        if (emailTaken)
            throw new FieldValidationException("email", "This email is already used by another student");

        // 2. Phone unique (only if provided)
        if (d.phone() != null && !d.phone().isBlank()) {
            boolean phoneTaken = creating
                    ? students.existsByPhone(d.phone())
                    : students.existsByPhoneAndIdNot(d.phone(), currentId);
            if (phoneTaken)
                throw new FieldValidationException("phone", "This phone number is already used by another student");
        }

        // 3. Same full name + same birth date = duplicate student
        if (d.birthDate() != null) {
            boolean duplicate = creating
                    ? students.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                    d.firstName().trim(), d.lastName().trim(), d.birthDate())
                    : students.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDateAndIdNot(
                    d.firstName().trim(), d.lastName().trim(), d.birthDate(), currentId);
            if (duplicate)
                throw new FieldValidationException("lastName",
                        "A student with the same name and birth date already exists");

            // 4. Realistic age
            int age = Period.between(d.birthDate(), LocalDate.now()).getYears();
            if (age < MIN_AGE)
                throw new FieldValidationException("birthDate", "Student must be at least " + MIN_AGE + " years old");
            if (age > MAX_AGE)
                throw new FieldValidationException("birthDate", "Birth date is not realistic");
        }

        // 5. Classroom exists and is not full
        if (d.classroomId() != null) {
            Classroom room = classrooms.findById(d.classroomId())
                    .orElseThrow(() -> new FieldValidationException("classroomId", "Classroom not found"));

            boolean sameRoomAsBefore = !creating && students.findById(currentId)
                    .map(s -> s.getClassroom() != null && s.getClassroom().getId().equals(room.getId()))
                    .orElse(false);

            if (!sameRoomAsBefore && room.getCapacity() != null
                    && students.countByClassroomId(room.getId()) >= room.getCapacity())
                throw new FieldValidationException("classroomId",
                        "Classroom " + room.getName() + " is full (" + room.getCapacity() + "/" + room.getCapacity() + ")");
        }
    }
}
