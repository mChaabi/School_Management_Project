package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.RiskStudentDto;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.repository.AttendanceRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import com.example.School.Management.System.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final AttendanceRepository attendanceRepo;

    @Value("${school.risk.min-average:10}")
    private double minAverage;

    @Value("${school.risk.max-absences:3}")
    private long maxAbsences;

    @Transactional(readOnly = true)
    public List<RiskStudentDto> findAtRisk() {
        Map<Long, Double> averages = new HashMap<>();
        for (Object[] r : enrollmentRepo.averageGradeByStudent()) {
            averages.put((Long) r[0], ((Number) r[1]).doubleValue());
        }

        Map<Long, Long> absences = new HashMap<>();
        for (Object[] r : attendanceRepo.countByStatusGroupedByStudent(AttendanceStatus.ABSENT)) {
            absences.put((Long) r[0], (Long) r[1]);
        }

        Set<Long> ids = new HashSet<>();
        averages.forEach((id, avg) -> { if (avg < minAverage) ids.add(id); });
        absences.forEach((id, n) -> { if (n >= maxAbsences) ids.add(id); });

        if (ids.isEmpty()) return List.of();

        return studentRepo.findAllById(ids).stream()
                .map(s -> {
                    Double avg = averages.get(s.getId());
                    long abs = absences.getOrDefault(s.getId(), 0L);
                    return new RiskStudentDto(
                            s.getId(),
                            s.getFirstName() + " " + s.getLastName(),
                            s.getClassroom() != null ? s.getClassroom().getName() : null,
                            avg,
                            abs,
                            avg != null && avg < minAverage,
                            abs >= maxAbsences);
                })
                .sorted(Comparator
                        .comparingInt((RiskStudentDto d) ->
                                (d.lowAverage() ? 1 : 0) + (d.manyAbsences() ? 1 : 0))
                        .reversed()
                        .thenComparing(d -> d.average() == null ? 99.0 : d.average()))
                .toList();
    }
}