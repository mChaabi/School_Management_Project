package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    List<Student> findByClassroomId(Long classroomId);

    @Query("SELECT s FROM Student s WHERE s.email = :username")
    Optional<Student> findByUserUsername(@Param("username") String username);

    Optional<Student> findByUser_Username(String username);

    // REMOVE findByParent_Username entirely because 'parent' does not exist on Student
}