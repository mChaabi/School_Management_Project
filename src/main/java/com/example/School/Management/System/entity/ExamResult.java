package com.example.School.Management.System.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;                       // null only while absent

    @Column(nullable = false)
    private boolean absent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;
}