package com.example.School.Management.System.repository;

import com.example.School.Management.System.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop20ByRecipientUsernameOrderByCreatedAtDesc(String username);

    long countByRecipientUsernameAndSeenFalse(String username);

    boolean existsByRecipientIdAndDedupKey(Long recipientId, String dedupKey);

    @Modifying
    @Transactional
    @Query("update Notification n set n.seen = true where n.recipient.username = :u and n.seen = false")
    int markAllSeen(@Param("u") String username);

    @EntityGraph(attributePaths = "recipient")
    List<Notification> findTop30ByOrderByCreatedAtDesc();

    // all notifications of one user, newest first (paged)
    Page<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    // used by the "open" action: only the owner can mark their own notification
    @Modifying
    @Transactional
    @Query("update Notification n set n.seen = true where n.id = :id and n.recipient.username = :u")
    int markSeen(@Param("id") Long id, @Param("u") String username);


}
