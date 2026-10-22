package com.example.School.Management.System.service;

import com.example.School.Management.System.enums.FeeStatus;
import com.example.School.Management.System.repository.FeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FeeReminderJob {

    private final FeeRepository feeRepo;
    private final NotificationService notifications;

    @Scheduled(cron = "0 0 8 * * *")          // cada día a las 08:00
    public void remindOverdue() {
        feeRepo.findOverdue(FeeStatus.PENDING, LocalDate.now()).forEach(f ->
                notifications.onFeeOverdue(f.getId(), f.getStudent().getId(),
                        f.getTitle(), f.getAmount(), f.getDueDate()));
    }
}
