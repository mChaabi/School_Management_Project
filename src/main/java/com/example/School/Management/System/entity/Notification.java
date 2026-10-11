package com.example.School.Management.System.entity;

import com.example.School.Management.System.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = @Index(name = "idx_notif_recipient", columnList = "recipient_id, created_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;              // plain text, no emoji

    private String link;                 // where the click leads

    @Column(name = "is_seen", nullable = false)   // "read" is a reserved word in MySQL
    private boolean seen;

    @Column(nullable = false)
    private boolean aiGenerated;         // useful for audits and for measuring AI vs fallback

    @Column(length = 120)
    private String dedupKey;             // e.g. "ABSENCE:12:2026-10-11"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
