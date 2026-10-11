package com.example.School.Management.System.api;

import com.example.School.Management.System.entity.Notification;
import com.example.School.Management.System.enums.AttendanceStatus;
import com.example.School.Management.System.repository.NotificationRepository;
import com.example.School.Management.System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notifications;
    private final NotificationRepository repo;

    // DTO: avoids returning the entity (lazy User, password hash...)
    public record NotificationView(Long id, String type, String message, String link,
                                   boolean seen, boolean aiGenerated, LocalDateTime createdAt) {
        static NotificationView of(Notification n) {
            return new NotificationView(n.getId(), n.getType().name(), n.getMessage(), n.getLink(),
                    n.isSeen(), n.isAiGenerated(), n.getCreatedAt());
        }
    }

    // 1. My notifications
    @GetMapping
    public List<NotificationView> mine(@RequestParam(defaultValue = "0") int page,
                                       Principal principal) {
        // If no one is logged in, use a default test username so it doesn't crash
        String username = (principal != null) ? principal.getName() : "admin"; // Change "admin" to an existing username in your DB

        return repo.findByRecipientUsernameOrderByCreatedAtDesc(username, PageRequest.of(page, 20))
                .map(NotificationView::of).getContent();
    }

    // 2. Unread counter
    @GetMapping("/unread-count")
    public Map<String, Long> unread(Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        return Map.of("unread", repo.countByRecipientUsernameAndSeenFalse(username));
    }

    // 3. Mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> read(@PathVariable Long id, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        int updated = repo.markSeen(id, username);
        return updated == 0
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(Map.of("id", id, "seen", true));
    }

    // 4. Mark all as read
    @PatchMapping("/read-all")
    public Map<String, Integer> readAll(Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        return Map.of("updated", repo.markAllSeen(username));
    }

    // 5. Simulate an attendance event (admin only, see security rule below)
    public record AttendanceEvent(Long studentId, AttendanceStatus status, String remark,
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {}

    @PostMapping("/dev/attendance")
    public ResponseEntity<Map<String, String>> attendance(@RequestBody AttendanceEvent e) {
        notifications.onAttendance(e.studentId(), e.date() != null ? e.date() : LocalDate.now(),
                e.status(), e.remark());
        return ResponseEntity.accepted().body(Map.of("status", "accepted",
                "note", "Processed async. Call GET /api/notifications as the recipient."));
    }
}
