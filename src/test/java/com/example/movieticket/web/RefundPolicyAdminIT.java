package com.example.movieticket.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.movieticket.repository.RefundPolicyRepository;
import com.example.movieticket.support.JsonFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class RefundPolicyAdminIT extends AbstractApiIT {

    @Autowired
    private RefundPolicyRepository refundPolicyRepository;

    @BeforeEach
    void setUp() {
        refundPolicyRepository.deleteAll();
        userRepository.deleteAll();
        seedAdmin();
    }

    private void createPolicy(String token) throws Exception {
        mockMvc.perform(post("/api/admin/refund-policies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("refundpolicy/request/create-policy.json")))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCreatesRefundPolicy() throws Exception {
        String response = mockMvc.perform(post("/api/admin/refund-policies")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("refundpolicy/request/create-policy.json")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonFixtures.assertMatches("refundpolicy/response/policy-created.json", response, "id");
    }

    @Test
    void duplicateThresholdReturnsConflict() throws Exception {
        String token = adminToken();
        createPolicy(token);

        mockMvc.perform(post("/api/admin/refund-policies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("refundpolicy/request/create-policy.json")))
                .andExpect(status().isConflict());
    }

    @Test
    void creatingRefundPolicyRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/refund-policies")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFixtures.read("refundpolicy/request/create-policy.json")))
                .andExpect(status().isForbidden());
    }
}
