package com.example.School.Management.System.controller;

import com.example.School.Management.System.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationCountAdvice {

    private final NotificationRepository repo;

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        return principal == null ? 0 : repo.countByRecipientUsernameAndSeenFalse(principal.getName());
    }
}
