package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // Check if a classroom name already exists (useful for validations)
    boolean existsByName(String name);

    // Check if another classroom with the same name exists (useful during updates)
    boolean existsByNameAndIdNot(String name, Long id);
}
