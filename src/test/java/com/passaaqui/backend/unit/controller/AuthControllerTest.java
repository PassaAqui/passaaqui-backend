package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.auth.controller.AuthController;
import com.passaaqui.backend.modules.auth.dto.LoginDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterShopkeeperDTO;
import com.passaaqui.backend.modules.auth.dto.RegisterTouristDTO;
import com.passaaqui.backend.modules.auth.service.AuthService;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.shared.objects.JWTObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService service;

    @InjectMocks
    private AuthController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerAccountTourist_shouldReturn201() throws Exception {
        RegisterTouristDTO dto = new RegisterTouristDTO("test@test.com", "Test User", "Str0ng!pass", "Str0ng!pass", "52998224725");
        TouristModel tourist = new TouristModel();
        tourist.setId(1);
        tourist.setEmail("test@test.com");
        tourist.setName("Test User");

        when(service.registerAccountTourist(any(RegisterTouristDTO.class))).thenReturn(tourist);

        mockMvc.perform(post("/api/auth/register/tourist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void registerAccountTourist_shouldReturn400WhenInvalid() throws Exception {
        RegisterTouristDTO dto = new RegisterTouristDTO("invalid", "", "", "", "");

        mockMvc.perform(post("/api/auth/register/tourist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerAccountShopkeeper_shouldReturn201() throws Exception {
        RegisterShopkeeperDTO dto = new RegisterShopkeeperDTO("shop@test.com", "Shop", "Str0ng!pass", "Str0ng!pass", "11222333000181", "Company", "desc", 1, "My Store", "Store desc", -23.5, -46.6, -23.6, -23.4, -46.7, -46.5, 1);
        ShopkeeperModel shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(2);
        shopkeeper.setEmail("shop@test.com");
        shopkeeper.setName("Shop");

        when(service.registerAccountShopkeeper(any(RegisterShopkeeperDTO.class), any())).thenReturn(shopkeeper);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/auth/register/shopkeeper")
                        .file(dataPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void registerAccountShopkeeper_shouldReturn400WhenInvalid() throws Exception {
        RegisterShopkeeperDTO dto = new RegisterShopkeeperDTO("bad", "", "", "", "", "", "", null, "", "", null, null, null, null, null, null, null);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/auth/register/shopkeeper")
                        .file(dataPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200() throws Exception {
        LoginDTO dto = new LoginDTO("test@test.com", "password");
        JWTObject tokens = new JWTObject();
        tokens.setAccess_token("access");
        tokens.setRefresh_token("refresh");

        when(service.loginAccount(eq("test@test.com"), eq("password"), anyString(), anyString())).thenReturn(tokens);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("User-Agent", "test-agent")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access"))
                .andExpect(jsonPath("$.refresh_token").value("refresh"));
    }

    @Test
    void login_shouldReturn400WhenInvalid() throws Exception {
        LoginDTO dto = new LoginDTO("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400WhenCredentialsInvalid() throws Exception {
        LoginDTO dto = new LoginDTO("test@test.com", "wrong");
        when(service.loginAccount(eq("test@test.com"), eq("wrong"), anyString(), anyString()))
                .thenThrow(new InvalidRequestException("Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("User-Agent", "agent")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturn200() throws Exception {
        JWTObject tokens = new JWTObject();
        tokens.setAccess_token("new-access");
        tokens.setRefresh_token("new-refresh");

        when(service.refreshToken(eq("valid-refresh"), anyString(), anyString())).thenReturn(tokens);

        mockMvc.perform(get("/api/auth/refresh")
                        .header("Authorization", "Bearer valid-refresh")
                        .header("User-Agent", "agent")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh"));
    }

    @Test
    void refresh_shouldReturn404WhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/auth/refresh")
                        .header("User-Agent", "agent")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    void refresh_shouldReturn400WhenTokenExpired() throws Exception {
        when(service.refreshToken(eq("bad-token"), anyString(), anyString()))
                .thenThrow(new InvalidRequestException("Invalid or expired refresh token."));

        mockMvc.perform(get("/api/auth/refresh")
                        .header("Authorization", "Bearer bad-token")
                        .header("User-Agent", "agent")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldReturn200() throws Exception {
        doNothing().when(service).logout(anyString());

        mockMvc.perform(get("/api/auth/logout")
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_shouldReturn200EvenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}
