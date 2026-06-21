package com.example.movieticket.service;

import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.Role;
import com.example.movieticket.exception.EmailAlreadyExistsException;
import com.example.movieticket.exception.InvalidCredentialsException;
import com.example.movieticket.mapper.UserMapper;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.security.JwtService;
import com.example.movieticket.web.dto.AuthResponse;
import com.example.movieticket.web.dto.UserResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    /** Self-registration always creates a CUSTOMER; admin accounts are provisioned separately. */
    @Transactional
    public UserResponse register(String email, String rawPassword) {
        String normalizedEmail = normalize(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }
        User user = new User(normalizedEmail, passwordEncoder.encode(rawPassword), Role.CUSTOMER);
        User saved = userRepository.save(user);
        log.info("Registered user id={} role={}", saved.getId(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        String normalizedEmail = normalize(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Failed login attempt for email={}", normalizedEmail);
            throw new InvalidCredentialsException();
        }
        log.info("User logged in id={}", user.getId());
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .tokenType(TOKEN_TYPE)
                .expiresInMinutes(jwtService.getExpirationMinutes())
                .build();
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
