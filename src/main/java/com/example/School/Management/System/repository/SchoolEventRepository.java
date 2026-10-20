package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.SchoolEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

// SchoolEventRepository
public interface SchoolEventRepository extends JpaRepository<SchoolEvent, Long> {
    // un evento se solapa con [from, to] si empieza antes del fin y termina después del inicio
    List<SchoolEvent> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate to, LocalDate from);
}
