package com.passaaqui.backend.infra.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryRateLimiterServiceImpl rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService.clear();
    }

    @Test
    @DisplayName("Should consume tokens and report remaining tokens correctly")
    void shouldConsumeTokensCorrectly() {
        String key = "test-client-1";

        ConsumptionProbe probe1 = rateLimiterService.tryConsume(key, RateLimitTier.CHECKIN);
        assertTrue(probe1.isConsumed());
        assertEquals(4, probe1.getRemainingTokens());

        for (int i = 0; i < 4; i++) {
            assertTrue(rateLimiterService.tryConsume(key, RateLimitTier.CHECKIN).isConsumed());
        }

        ConsumptionProbe exceededProbe = rateLimiterService.tryConsume(key, RateLimitTier.CHECKIN);
        assertFalse(exceededProbe.isConsumed());
        assertEquals(0, exceededProbe.getRemainingTokens());
        assertTrue(exceededProbe.getNanosToWaitForRefill() > 0);
    }

    @Test
    @DisplayName("Should return 429 Too Many Requests when rate limit is exceeded on endpoint")
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        String loginPayload = "{\"email\":\"invalid@email.com\",\"password\":\"wrongpassword\"}";

        for (int i = 0; i < RateLimitTier.AUTH.getCapacity(); i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload)
                            .with(req -> {
                                req.setRemoteAddr("192.168.1.100");
                                return req;
                            }))
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload)
                        .with(req -> {
                            req.setRemoteAddr("192.168.1.100");
                            return req;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-Rate-Limit-Remaining", "0"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").exists());
    }
}
