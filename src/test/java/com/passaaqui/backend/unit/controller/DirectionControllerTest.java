package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.modules.direction.controller.DirectionController;
import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.service.DirectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DirectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DirectionService service;

    @InjectMocks
    private DirectionController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(new org.springframework.security.core.context.SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("1", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TOURIST")))
        ));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getDirection_shouldReturn200() throws Exception {
        var dto = new DirectionRequestDTO("driving-car", -46.6576, -23.5874, -46.6333, -23.5505);
        var expected = Map.of("routes", "data");

        when(service.getDirections(any(DirectionRequestDTO.class), anyString())).thenReturn(expected);

        mockMvc.perform(post("/api/direction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes").value("data"));
    }

    @Test
    void getDirection_shouldReturn400WhenModeBlank() throws Exception {
        var dto = new DirectionRequestDTO("", 0, 0, 0, 0);

        mockMvc.perform(post("/api/direction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDirection_shouldReturn400WhenInvalidMode() throws Exception {
        var dto = new DirectionRequestDTO("bad-mode", 0, 0, 0, 0);

        when(service.getDirections(any(DirectionRequestDTO.class), anyString()))
                .thenThrow(new InvalidRequestException("Invalid driving mode: bad-mode"));

        mockMvc.perform(post("/api/direction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
