package com.passaaqui.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.user.controller.UserController;
import com.passaaqui.backend.modules.user.dto.UpdateUserDTO;
import com.passaaqui.backend.modules.user.model.UserModel;
import com.passaaqui.backend.modules.user.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService service;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private UserController controller;

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
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setEmail("user@test.com");
        user.setName("User");

        when(service.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    @Test
    void findAll_shouldReturn200WithImageUrl() throws Exception {
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setEmail("user@test.com");
        user.setName("User");
        user.setImage("img.jpg");

        when(service.findAll()).thenReturn(List.of(user));
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].image").value("http://storage.com/img.jpg"));
    }

    @Test
    void findByIdentifier_shouldReturn200() throws Exception {
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setEmail("user@test.com");
        user.setName("User");

        when(service.findByIdOrEmail("1")).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByIdentifier_shouldReturn200WithImageUrl() throws Exception {
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setEmail("user@test.com");
        user.setName("User");
        user.setImage("img.jpg");

        when(service.findByIdOrEmail("1")).thenReturn(user);
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/img.jpg"));
    }

    @Test
    void findByIdentifier_shouldReturn404WhenNotFound() throws Exception {
        when(service.findByIdOrEmail("999")).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO("Updated Name");
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setName("Updated Name");
        user.setEmail("user@test.com");

        when(service.update(eq(1), any(UpdateUserDTO.class), any())).thenReturn(user);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/users/1")
                        .file(dataPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void update_shouldReturn200WithImage() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO("Updated Name");
        UserModel user = new UserModel() {};
        user.setId(1);
        user.setName("Updated Name");
        user.setEmail("user@test.com");
        user.setImage("new-img.jpg");

        when(service.update(eq(1), any(UpdateUserDTO.class), any())).thenReturn(user);
        when(storageService.getFileUrl("new-img.jpg")).thenReturn("http://storage.com/new-img.jpg");

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));
        MockMultipartFile imagePart = new MockMultipartFile("image", "img.jpg", "image/jpeg", "image-data".getBytes());

        mockMvc.perform(multipart("/api/users/1")
                        .file(dataPart)
                        .file(imagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/new-img.jpg"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO("Updated Name");
        when(service.update(eq(999), any(UpdateUserDTO.class), any()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/users/999")
                        .file(dataPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }
}
