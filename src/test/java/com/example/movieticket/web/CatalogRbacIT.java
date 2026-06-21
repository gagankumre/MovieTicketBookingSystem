package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.config.AdminProperties;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.Role;
import com.example.movieticket.repository.CityRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.support.JsonFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogRbacIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminProperties adminProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cityRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User(adminProperties.email().toLowerCase(),
                passwordEncoder.encode(adminProperties.password()), Role.ADMIN));
    }

    private String token(String loginFixture) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read(loginFixture)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String adminToken() throws Exception {
        return token("auth/request/login-admin.json");
    }

    private String customerToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("auth/request/register-valid.json")))
                .andExpect(status().isCreated());
        return token("auth/request/login-valid.json");
    }

    private void createCityAsAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/admin/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void adminEndpointForbiddenForCustomer() throws Exception {
        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminCreatesCity() throws Exception {
        String response = mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("city/response/city-created.json", response, "id");
        assertThat(cityRepository.existsByNameIgnoreCase("Bengaluru")).isTrue();
    }

    @Test
    void publicListsCitiesWithoutAuthentication() throws Exception {
        createCityAsAdmin();

        mockMvc.perform(get("/api/public/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bengaluru"));
    }

    @Test
    void duplicateCityReturnsConflict() throws Exception {
        createCityAsAdmin();

        mockMvc.perform(post("/api/admin/cities")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("city/request/create-city.json")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
