package com.example.School.Management.System.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Async
    public void sendActivation(String to, String username, String link) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject("Activate your EduManage account");
            h.setText("""
                <div style="font-family:Arial,sans-serif;max-width:480px">
                  <h2 style="color:#4f46e5">EduManage</h2>
                  <p>Hello <strong>%s</strong>,</p>
                  <p>An account was created for you. Click the button to activate it:</p>
                  <p><a href="%s" style="background:#4f46e5;color:#fff;padding:12px 20px;
                     border-radius:8px;text-decoration:none;display:inline-block">Activate account</a></p>
                  <p style="color:#64748b;font-size:13px">This link expires in 24 hours.
                     If you did not expect this email, ignore it.</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(username), link), true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Could not send activation email to {}: {}", to, e.getMessage());
        }
    }
}
