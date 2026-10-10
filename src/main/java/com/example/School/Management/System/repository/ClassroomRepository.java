package com.example.School.Management.System.repository;

import com.example.School.Management.System.dto.LabelCount;
import com.example.School.Management.System.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // Check if a classroom name already exists (useful for validations)
    boolean existsByName(String name);

    // Check if another classroom with the same name exists (useful during updates)
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("select c.name as label, count(s) as total from Classroom c left join c.students s group by c.id, c.name order by c.name")
    List<LabelCount> countStudentsPerClassroom();
}
