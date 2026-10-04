package com.example.School.Management.System.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;          // e.g. "Grade 5-A"

    private Integer capacity;

    @OneToMany(mappedBy = "classroom")
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}
