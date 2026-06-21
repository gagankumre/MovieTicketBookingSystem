package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.repository.DiscountCodeRepository;
import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class DiscountAdminIT extends AbstractApiIT {

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @BeforeEach
    void setUp() {
        discountCodeRepository.deleteAll();
        userRepository.deleteAll();
        seedAdmin();
    }

    private void createDiscount(String token) throws Exception {
        mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCreatesDiscountCode() throws Exception {
        String response = mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("discount/response/discount-created.json", response, "id");
    }

    @Test
    void duplicateCodeReturnsConflict() throws Exception {
        String token = adminToken();
        createDiscount(token);

        mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount.json")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidDiscountRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount-invalid.json")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creatingDiscountRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/discount-codes")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("discount/request/create-discount.json")))
                .andExpect(status().isForbidden());
    }
}
