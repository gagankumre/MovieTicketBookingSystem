package com.example.movieticket.config;

import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.Role;
import com.example.movieticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Provisions the first ADMIN account from {@code app.admin.*} config if it does not already exist.
 * Self-registration only creates customers, so this is how admins are bootstrapped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        String email = adminProperties.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(new User(email, passwordEncoder.encode(adminProperties.password()), Role.ADMIN));
        log.info("Seeded default admin account: {}", email);
    }
}
