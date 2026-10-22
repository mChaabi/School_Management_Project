package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Fee;
import com.example.School.Management.System.enums.FeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    @Query(value = """
        select f from Fee f join fetch f.student s
        where (:status is null or f.status = :status)
          and (:k = '' or lower(s.firstName) like lower(concat('%', :k, '%'))
                       or lower(s.lastName)  like lower(concat('%', :k, '%'))
                       or lower(f.title)     like lower(concat('%', :k, '%')))
        """,
            countQuery = """
        select count(f) from Fee f join f.student s
        where (:status is null or f.status = :status)
          and (:k = '' or lower(s.firstName) like lower(concat('%', :k, '%'))
                       or lower(s.lastName)  like lower(concat('%', :k, '%'))
                       or lower(f.title)     like lower(concat('%', :k, '%')))
        """)
    Page<Fee> search(@Param("status") FeeStatus status, @Param("k") String keyword, Pageable pageable);

    @Query("select f from Fee f join fetch f.student where f.id = :id")
    Optional<Fee> findByIdWithStudent(@Param("id") Long id);

    @Query("select f from Fee f join fetch f.student where f.student.id in :ids order by f.dueDate desc")
    List<Fee> findByStudentIds(@Param("ids") Collection<Long> ids);

    @Query("select coalesce(sum(f.amount), 0) from Fee f where f.status = :status")
    BigDecimal sumByStatus(@Param("status") FeeStatus status);

    @Query("select coalesce(sum(f.amount), 0) from Fee f where f.status = :status and f.dueDate < :today")
    BigDecimal sumOverdue(@Param("status") FeeStatus status, @Param("today") LocalDate today);

    boolean existsByStudentIdAndTitleAndStatusNot(Long studentId, String title, FeeStatus status);

    // FeeRepository
    @Query("select f from Fee f join fetch f.student where f.status = :status and f.dueDate < :today")
    List<Fee> findOverdue(@Param("status") FeeStatus status, @Param("today") LocalDate today);
}