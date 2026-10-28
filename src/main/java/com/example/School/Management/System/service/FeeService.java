package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.FeeForm;
import com.example.School.Management.System.dto.FeeSummary;
import com.example.School.Management.System.entity.Fee;
import com.example.School.Management.System.entity.Student;
import com.example.School.Management.System.enums.FeeStatus;
import com.example.School.Management.System.enums.PaymentMethod;
import com.example.School.Management.System.exception.ResourceNotFoundException;
import com.example.School.Management.System.repository.FeeRepository;
import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRepository feeRepo;
    private final StudentRepository studentRepo;
    private final StudentAccessService access;

    /** Devuelve cuántas cuotas se crearon. Los mensajes de error son claves i18n. */
    /** Devuelve cuántas cuotas se crearon. Los mensajes de error son claves i18n. */
    @Transactional
    public List<Fee> create(FeeForm f) {
        boolean hasStudent = f.studentId() != null;
        boolean hasRoom = f.classroomId() != null;
        if (hasStudent == hasRoom) throw new IllegalArgumentException("fees.error.target");

        List<Student> targets = hasStudent
                ? List.of(studentRepo.findById(f.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("fees.error.noStudents")))
                : studentRepo.findByClassroomId(f.classroomId());
        if (targets.isEmpty()) throw new IllegalArgumentException("fees.error.noStudents");

        String title = f.title().trim();
        List<Fee> createdList = new ArrayList<>(); // Lista para guardar las cuotas creadas

        for (Student s : targets) {
            if (feeRepo.existsByStudentIdAndTitleAndStatusNot(s.getId(), title, FeeStatus.CANCELLED)) continue;

            Fee savedFee = feeRepo.save(Fee.builder()
                    .student(s)
                    .title(title)
                    .amount(f.amount().setScale(2, RoundingMode.HALF_UP))
                    .dueDate(f.dueDate())
                    .status(FeeStatus.PENDING)
                    .build());

            createdList.add(savedFee); // Añadimos cada cuota guardada a la lista
        }

        return createdList; // Retornamos la lista completa
    }

    @Transactional
    public Fee markPaid(Long id, PaymentMethod method) {
        Fee fee = feeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fee not found: " + id));
        if (fee.getStatus() != FeeStatus.PENDING) throw new IllegalStateException("fees.error.notPending");

        fee.setStatus(FeeStatus.PAID);
        fee.setMethod(method);
        fee.setPaidDate(LocalDate.now());
        fee.setReceiptNumber(String.format("R-%d-%06d", LocalDate.now().getYear(), fee.getId()));
        return feeRepo.save(fee);  }

    @Transactional
    public void cancel(Long id) {
        Fee fee = feeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fee not found: " + id));
        if (fee.getStatus() != FeeStatus.PENDING) throw new IllegalStateException("fees.error.notPending");
        fee.setStatus(FeeStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Page<Fee> search(FeeStatus status, String keyword, int page) {
        String k = keyword == null ? "" : keyword.trim();
        return feeRepo.search(status, k, PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "dueDate")));
    }

    @Transactional(readOnly = true)
    public FeeSummary summary() {
        return new FeeSummary(
                feeRepo.sumByStatus(FeeStatus.PENDING),
                feeRepo.sumOverdue(FeeStatus.PENDING, LocalDate.now()),
                feeRepo.sumByStatus(FeeStatus.PAID));
    }

    @Transactional(readOnly = true)
    public List<Fee> findForParent(String username) {
        List<Long> ids = studentRepo.findByParentUsername(username).stream().map(Student::getId).toList();
        return ids.isEmpty() ? List.of() : feeRepo.findByStudentIds(ids);
    }

    /** Solo cuotas pagadas y solo para quien tenga permiso sobre ese estudiante. */
    @Transactional(readOnly = true)
    public Fee getPaidFeeForReceipt(Long id, Authentication auth) {
        Fee fee = feeRepo.findByIdWithStudent(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found: " + id));
        if (!access.canViewFinance(fee.getStudent().getId(), auth)) {
            throw new AccessDeniedException("Not allowed");
        }
        if (fee.getStatus() != FeeStatus.PAID) throw new ResourceNotFoundException("Receipt not available");
        return fee;
    }
}
