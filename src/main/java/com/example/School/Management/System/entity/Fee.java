package com.example.School.Management.System.entity;

import com.example.School.Management.System.enums.FeeStatus;
import com.example.School.Management.System.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fees", indexes = @Index(columnList = "student_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false, length = 120)
    private String title;                       // "Matrícula 2026", "Mensualidad octubre"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FeeStatus status;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PaymentMethod method;

    @Column(name = "receipt_number", unique = true, length = 30)
    private String receiptNumber;

    /** Vencida = pendiente y con fecha límite pasada. Se calcula, no se guarda. */
    public boolean isOverdue() {
        return status == FeeStatus.PENDING && dueDate.isBefore(LocalDate.now());
    }
}
