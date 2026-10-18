package com.example.School.Management.System.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id") private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT") private String text;
    private int position;

    @Lob @Column(name = "image", columnDefinition = "LONGBLOB") private byte[] image;
    @Column(name = "image_type", length = 100) private String imageType;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuizOption> options = new ArrayList<>();
}