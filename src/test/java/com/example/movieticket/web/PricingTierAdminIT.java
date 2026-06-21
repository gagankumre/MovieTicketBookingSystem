package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PricingTierAdminIT extends AbstractCatalogIT {

    private void createTier(String token) throws Exception {
        mockMvc.perform(post("/api/admin/pricing-tiers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("pricingtier/request/create-tier.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCreatesPricingTier() throws Exception {
        String response = mockMvc.perform(post("/api/admin/pricing-tiers")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("pricingtier/request/create-tier.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("pricingtier/response/tier-created.json", response, "id");
    }

    @Test
    void duplicateTierReturnsConflict() throws Exception {
        String token = adminToken();
        createTier(token);

        mockMvc.perform(post("/api/admin/pricing-tiers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("pricingtier/request/create-tier.json")))
                .andExpect(status().isConflict());
    }

    @Test
    void creatingPricingTierRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/pricing-tiers")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("pricingtier/request/create-tier.json")))
                .andExpect(status().isForbidden());
    }
}
