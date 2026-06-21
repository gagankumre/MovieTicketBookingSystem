package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.config.AdminProperties;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.Role;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.support.JsonFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared base for API integration tests: full context on H2, plus helpers for seeding the admin
 * and obtaining JWTs. Subclasses handle their own per-test data cleanup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractApiIT {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected AdminProperties adminProperties;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    protected void seedAdmin() {
        userRepository.save(new User(adminProperties.email().toLowerCase(),
                passwordEncoder.encode(adminProperties.password()), Role.ADMIN));
    }

    protected String token(String loginFixture) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read(loginFixture)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    protected String adminToken() throws Exception {
        return token("auth/request/login-admin.json");
    }

    protected String customerToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-valid.json")))
                .andExpect(status().isCreated());
        return token("auth/request/login-valid.json");
    }

    protected long idOf(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
