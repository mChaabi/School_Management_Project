package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String firstName, String lastName, String email, String phone, Pageable pageable);

    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(String f, String l, LocalDate b);
    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDateAndIdNot(String f, String l, LocalDate b, Long id);

    long countByClassroomId(Long classroomId);

    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    List<Student> findByClassroomId(Long classroomId);
    List<Student> findByClassroomIsNull();

    @Query("SELECT s FROM Student s WHERE s.email = :username")
    Optional<Student> findByUserUsername(@Param("username") String username);

    Optional<Student> findByUser_Username(String username);

    @Query("select s from Student s left join fetch s.classroom order by s.lastName, s.firstName")
    List<Student> findAllForExport();

    // StudentRepository
    @Query("select s from Student s left join fetch s.classroom left join fetch s.user left join fetch s.parent where s.id = :id")
    Optional<Student> findForNotification(@Param("id") Long id);

    @Query("select s from Student s left join fetch s.user left join fetch s.parent where s.classroom.id = :classroomId")
    List<Student> findByClassroomForNotification(@Param("classroomId") Long classroomId);



    @Query("""
  select s from Student s
  where lower(s.firstName) like lower(concat('%', :k, '%'))
     or lower(s.lastName)  like lower(concat('%', :k, '%'))
     or lower(s.email)     like lower(concat('%', :k, '%'))
""")
    Page<Student> searchAll(@Param("k") String keyword, Pageable pageable);
}