package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.UserRegistrationDto;
import com.example.School.Management.System.entity.ActivationToken;
import com.example.School.Management.System.entity.User;
import com.example.School.Management.System.repository.ActivationTokenRepository;
import com.example.School.Management.System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountActivationService {

    private static final Duration VALIDITY = Duration.ofHours(24);
    private static final Duration COOLDOWN = Duration.ofMinutes(2);

    private final UserService userService;
    private final UserRepository userRepo;
    private final ActivationTokenRepository tokenRepo;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    private final SecureRandom random = new SecureRandom();

    /** Registra el usuario, lo deja bloqueado y envía el email. */
    @Transactional
    public void registerAndSend(UserRegistrationDto dto) {
        userService.registerUser(dto);
        User user = userRepo.findByUsername(dto.username()).orElseThrow();
        user.setEnabled(false);
        sendActivation(user);
    }

    /** Valida el token. Devuelve true si la cuenta se activó. */
    @Transactional
    public boolean activate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        Optional<ActivationToken> found = tokenRepo.findByTokenHash(hash(rawToken));
        if (found.isEmpty() || found.get().getExpiresAt().isBefore(LocalDateTime.now())) return false;

        ActivationToken t = found.get();
        t.getUser().setEnabled(true);
        tokenRepo.delete(t);              // un solo uso
        return true;
    }

    /** Reenvío. Siempre silencioso: no revela si el email existe. */
    @Transactional
    public void resend(String email) {
        if (email == null || email.isBlank()) return;
        for (User u : userRepo.findAllByEmailIgnoreCase(email.trim())) {
            // Solo si está PENDIENTE: tiene un token guardado. Una cuenta desactivada
            // por el admin no tiene token y no puede reactivarse por aquí.
            Optional<ActivationToken> last = tokenRepo.findFirstByUserOrderByCreatedAtDesc(u);
            if (u.isEnabled() || last.isEmpty()) continue;
            if (last.get().getCreatedAt().plus(COOLDOWN).isAfter(LocalDateTime.now())) continue;
            sendActivation(u);
        }
    }

    private void sendActivation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) return;

        tokenRepo.deleteByUser(user);                      // invalida tokens anteriores
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime now = LocalDateTime.now();
        tokenRepo.save(new ActivationToken(null, hash(raw), user, now.plus(VALIDITY), now));

        emailService.sendActivation(user.getEmail(), user.getUsername(),
                baseUrl + "/activate?token=" + raw);
    }

    private String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
