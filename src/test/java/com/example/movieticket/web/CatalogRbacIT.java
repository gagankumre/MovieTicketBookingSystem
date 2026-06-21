package com.example.movieticket.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CatalogRbacIT extends AbstractCatalogIT {

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
