package com.passaaqui.backend.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.modules.auth.dto.LoginDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserJourneyAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullUserJourney_shouldRegisterLoginAndAccessProtectedResource() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        String baseUrl = "http://localhost:" + port;

        String email = "turista@teste.com";
        String password = "Test@1234";
        String name = "João Turista";

        RegisterTouristDTO registerDTO = new RegisterTouristDTO(
                email, name, password, password, "52998224725"
        );

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register/tourist"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerDTO)))
                .build();

        HttpResponse<String> registerResponse = httpClient.send(registerRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, registerResponse.statusCode());
        JsonNode registerBody = objectMapper.readTree(registerResponse.body());
        assertEquals("TOURIST", registerBody.get("role").asText());
        assertEquals(email, registerBody.get("email").asText());
        assertEquals(name, registerBody.get("name").asText());
        assertTrue(registerBody.get("id").isInt());

        LoginDTO loginDTO = new LoginDTO(email, password);

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginDTO)))
                .build();

        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, loginResponse.statusCode());
        JsonNode loginBody = objectMapper.readTree(loginResponse.body());
        String accessToken = loginBody.get("access_token").asText();
        assertNotNull(accessToken);
        assertFalse(accessToken.isBlank());
        assertNotNull(loginBody.get("refresh_token").asText());

        HttpRequest profileRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tourists/me"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> profileResponse = httpClient.send(profileRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, profileResponse.statusCode());
        JsonNode profileBody = objectMapper.readTree(profileResponse.body());
        assertEquals(email, profileBody.get("email").asText());
        assertEquals(name, profileBody.get("name").asText());
        assertTrue(profileBody.get("id").isInt());
    }
}
