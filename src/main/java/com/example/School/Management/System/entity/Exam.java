package com.example.School.Management.System.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;                       // "Midterm", "Quiz 1"

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "max_score", nullable = false)
    private Double maxScore;                    // 20, 10, 100...

    @Column(nullable = false)
    private Integer weight;                     // importance in percent

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExamResult> results = new ArrayList<>();
}