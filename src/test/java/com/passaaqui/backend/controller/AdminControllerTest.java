package com.passaaqui.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.modules.admin.controller.AdminController;
import com.passaaqui.backend.modules.admin.dto.UpdateAdminDTO;
import com.passaaqui.backend.modules.admin.model.AdminModel;
import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.admin.service.AdminService;
import com.passaaqui.backend.modules.auth.dto.RegisterAdminDTO;
import com.passaaqui.backend.modules.auth.service.AuthService;
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
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void create_shouldReturn200() throws Exception {
        RegisterAdminDTO dto = new RegisterAdminDTO("admin@test.com", "Admin", "Str0ng!pass", "Str0ng!pass", AdminType.USER);
        AdminModel admin = new AdminModel();
        admin.setId(1);
        admin.setEmail("admin@test.com");
        admin.setAdminType(AdminType.USER);

        when(authService.registerAccountAdmin(any(RegisterAdminDTO.class))).thenReturn(admin);

        mockMvc.perform(post("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void create_shouldReturn400WhenInvalid() throws Exception {
        RegisterAdminDTO dto = new RegisterAdminDTO("", "", "", "", null);

        mockMvc.perform(post("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409WhenEmailConflict() throws Exception {
        RegisterAdminDTO dto = new RegisterAdminDTO("existing@test.com", "Admin", "Str0ng!pass", "Str0ng!pass", AdminType.USER);
        when(authService.registerAccountAdmin(any(RegisterAdminDTO.class)))
                .thenThrow(new ConflictException("The administrator email is already in use."));

        mockMvc.perform(post("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        AdminModel admin = new AdminModel();
        admin.setId(1);
        admin.setEmail("admin@test.com");

        when(adminService.getAll()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        AdminModel admin = new AdminModel();
        admin.setId(1);
        admin.setEmail("admin@test.com");

        when(adminService.getOneById(1)).thenReturn(admin);

        mockMvc.perform(get("/api/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(adminService.getOneById(999)).thenThrow(new ResourceNotFoundException("Admin not found with id: 999"));

        mockMvc.perform(get("/api/admin/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByEmail_shouldReturn200() throws Exception {
        AdminModel admin = new AdminModel();
        admin.setId(1);
        admin.setEmail("admin@test.com");

        when(adminService.getOneByEmail("admin@test.com")).thenReturn(admin);

        mockMvc.perform(get("/api/admin/email")
                        .param("email", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void getByEmail_shouldReturn404WhenNotFound() throws Exception {
        when(adminService.getOneByEmail("nonexistent@test.com"))
                .thenThrow(new ResourceNotFoundException("Admin not found with email: nonexistent@test.com"));

        mockMvc.perform(get("/api/admin/email")
                        .param("email", "nonexistent@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateAdminDTO dto = new UpdateAdminDTO("updated@test.com", "Updated", "Str0ng!pass", AdminType.USER);
        AdminModel admin = new AdminModel();
        admin.setId(1);
        admin.setEmail("updated@test.com");
        admin.setName("Updated");

        when(adminService.updateAdmin(eq(1), eq("updated@test.com"), eq("Updated"), eq("Str0ng!pass"), eq(AdminType.USER)))
                .thenReturn(admin);

        mockMvc.perform(put("/api/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    void update_shouldReturn400WhenInvalid() throws Exception {
        UpdateAdminDTO dto = new UpdateAdminDTO("", "", "", null);

        mockMvc.perform(put("/api/admin/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateAdminDTO dto = new UpdateAdminDTO("updated@test.com", "Updated", null, AdminType.USER);
        when(adminService.updateAdmin(eq(999), anyString(), anyString(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Admin not found with id: 999"));

        mockMvc.perform(put("/api/admin/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(adminService).deleteAdmin(1);

        mockMvc.perform(delete("/api/admin/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Admin not found with id: 999"))
                .when(adminService).deleteAdmin(999);

        mockMvc.perform(delete("/api/admin/999"))
                .andExpect(status().isNotFound());
    }
}
