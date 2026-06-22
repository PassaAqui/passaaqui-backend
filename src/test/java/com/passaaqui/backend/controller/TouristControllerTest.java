package com.passaaqui.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.modules.tourist.controller.TouristController;
import com.passaaqui.backend.modules.tourist.dto.UpdateTouristDTO;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.service.TouristService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TouristControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TouristService service;

    @InjectMocks
    private TouristController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        TouristModel tourist = new TouristModel();
        tourist.setId(1);
        tourist.setEmail("tourist@test.com");
        tourist.setName("Tourist");

        when(service.findAll()).thenReturn(List.of(tourist));

        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("tourist@test.com"));
    }

    @Test
    void findAll_shouldReturn200EmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tourists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findByIdentifier_shouldReturn200() throws Exception {
        TouristModel tourist = new TouristModel();
        tourist.setId(1);
        tourist.setEmail("tourist@test.com");

        when(service.findByIdOrEmail("1")).thenReturn(tourist);

        mockMvc.perform(get("/api/tourists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByIdentifier_shouldReturn200ByEmail() throws Exception {
        TouristModel tourist = new TouristModel();
        tourist.setId(1);
        tourist.setEmail("tourist@test.com");

        when(service.findByIdOrEmail("tourist@test.com")).thenReturn(tourist);

        mockMvc.perform(get("/api/tourists/tourist@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tourist@test.com"));
    }

    @Test
    void findByIdentifier_shouldReturn404WhenNotFound() throws Exception {
        when(service.findByIdOrEmail("999")).thenThrow(new ResourceNotFoundException("Tourist not found"));

        mockMvc.perform(get("/api/tourists/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateTouristDTO dto = new UpdateTouristDTO("Updated", "Str0ng!pass", "12345678901");
        TouristModel tourist = new TouristModel();
        tourist.setId(1);
        tourist.setName("Updated");
        tourist.setDocumentId("12345678901");

        when(service.update(eq("1"), any(UpdateTouristDTO.class))).thenReturn(tourist);

        mockMvc.perform(put("/api/tourists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.documentId").value("12345678901"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateTouristDTO dto = new UpdateTouristDTO("Updated", "Str0ng!pass", "12345678901");
        when(service.update(eq("999"), any(UpdateTouristDTO.class)))
                .thenThrow(new ResourceNotFoundException("Tourist not found"));

        mockMvc.perform(put("/api/tourists/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn400WhenInvalid() throws Exception {
        UpdateTouristDTO dto = new UpdateTouristDTO("", "", "");

        mockMvc.perform(put("/api/tourists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete("1");

        mockMvc.perform(delete("/api/tourists/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Tourist not found")).when(service).delete("999");

        mockMvc.perform(delete("/api/tourists/999"))
                .andExpect(status().isNotFound());
    }
}
