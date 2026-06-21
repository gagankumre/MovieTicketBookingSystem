package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    private void registerValidUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-valid.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void registerCreatesCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-valid.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/register-created.json", response, "id");
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmailWithConflict() throws Exception {
        registerValidUser();

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-valid.json")))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/register-conflict.json", response);
    }

    @Test
    void registerRejectsInvalidInputWithFieldErrors() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-invalid.json")))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/register-validation-error.json", response,
                "fieldErrors.email", "fieldErrors.password");
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        registerValidUser();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/login-valid.json")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/login-success.json", response, "token");
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        registerValidUser();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/login-wrong-password.json")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/login-unauthorized.json", response);
    }

    @Test
    void loginRejectsUnknownEmail() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/login-unknown.json")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("auth/response/login-unauthorized.json", response);
    }
}
