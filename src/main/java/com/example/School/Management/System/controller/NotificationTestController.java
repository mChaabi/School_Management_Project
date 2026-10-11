package com.example.School.Management.System.controller;

import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.repository.NotificationRepository;
import com.example.School.Management.System.repository.StudentRepository;
import com.example.School.Management.System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationService notifications;
    private final NotificationRepository notificationRepo;
    private final StudentRepository students;


    // The page opened by the bell
    @GetMapping("/notifications")
    public String list(@RequestParam(defaultValue = "0") int page,
                       Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("page", notificationRepo
                .findByRecipientUsernameOrderByCreatedAtDesc(username, PageRequest.of(page, 20)));
        model.addAttribute("unreadCount", notificationRepo.countByRecipientUsernameAndSeenFalse(username));
        return "dev/notification-list";          // your file: templates/dev/notification-list.html
    }


    // Click on a card: mark it as read, then go to its link
    @GetMapping("/notifications/{id}/open")
    public String open(@PathVariable Long id,
                       @RequestParam(defaultValue = "/notifications") String to,
                       Principal principal) {
        notificationRepo.markSeen(id, principal.getName());
        return "redirect:" + (to.startsWith("/") && !to.startsWith("//") ? to : "/notifications");
    }

    @PostMapping("/notifications/read-all")
    public String readAll(Principal principal) {
        notificationRepo.markAllSeen(principal.getName());
        return "redirect:/notifications";
    }

    @GetMapping("/dev/notifications")
    public String devPage(Model model) {
        model.addAttribute("notifications", notificationRepo.findTop30ByOrderByCreatedAtDesc());
        model.addAttribute("students", students.findAll());
        model.addAttribute("today", LocalDate.now());
        return "dev/notifications";
    }

    @PostMapping("/dev/notify-test")
    public String devSend(@RequestParam Long studentId,
                          @RequestParam AttendanceStatus status,
                          @RequestParam(required = false) String remark,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          RedirectAttributes ra) {
        notifications.onAttendance(studentId, date, status, remark);
        ra.addFlashAttribute("justSent", true);
        ra.addFlashAttribute("success", "Event sent. Notifications appear below in a moment.");
        return "redirect:/dev/notifications";
    }
}
