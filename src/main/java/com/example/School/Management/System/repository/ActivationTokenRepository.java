package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.ActivationToken;
import com.example.School.Management.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByTokenHash(String tokenHash);
    Optional<ActivationToken> findFirstByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}