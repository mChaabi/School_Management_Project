package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // UserRepository  (assumes User has: Set<Role> roles)
    @Query("select distinct u from User u join u.roles r where r = :role")
    List<User> findByRole(@Param("role") Role role);
}