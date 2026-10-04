package com.example.School.Management.System.entity;


import com.example.School.Management.System.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String specialization;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();
}
