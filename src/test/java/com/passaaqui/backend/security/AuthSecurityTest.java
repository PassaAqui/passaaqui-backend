package com.passaaqui.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authPublicEndpoints_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN_USER")
    void protectedEndpoint_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TOURIST")
    void touristEndpoint_shouldAllowTouristRole() throws Exception {
        mockMvc.perform(post("/api/pois/{poiId}/ratings", 1)
                        .contentType("application/json")
                        .content("{\"rating\":4}"))
                .andExpect(status().isOk());
    }
}
