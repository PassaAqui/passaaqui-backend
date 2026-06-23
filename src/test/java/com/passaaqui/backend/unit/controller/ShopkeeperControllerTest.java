package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.modules.shopkeeper.controller.ShopkeeperController;
import com.passaaqui.backend.modules.shopkeeper.dto.UpdateShopkeeperDTO;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.service.ShopkeeperService;
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
class ShopkeeperControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShopkeeperService service;

    @InjectMocks
    private ShopkeeperController controller;

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
        ShopkeeperModel shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(1);
        shopkeeper.setEmail("shop@test.com");
        shopkeeper.setName("Shop");

        when(service.findAll()).thenReturn(List.of(shopkeeper));

        mockMvc.perform(get("/api/shopkeepers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("shop@test.com"));
    }

    @Test
    void findAll_shouldReturn200EmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/shopkeepers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findByIdentifier_shouldReturn200() throws Exception {
        ShopkeeperModel shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(1);
        shopkeeper.setEmail("shop@test.com");

        when(service.findByIdOrEmail("1")).thenReturn(shopkeeper);

        mockMvc.perform(get("/api/shopkeepers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByIdentifier_shouldReturn200ByEmail() throws Exception {
        ShopkeeperModel shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(1);
        shopkeeper.setEmail("shop@test.com");

        when(service.findByIdOrEmail("shop@test.com")).thenReturn(shopkeeper);

        mockMvc.perform(get("/api/shopkeepers/shop@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("shop@test.com"));
    }

    @Test
    void findByIdentifier_shouldReturn404WhenNotFound() throws Exception {
        when(service.findByIdOrEmail("999")).thenThrow(new ResourceNotFoundException("Shopkeeper not found"));

        mockMvc.perform(get("/api/shopkeepers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateShopkeeperDTO dto = new UpdateShopkeeperDTO("Updated", "Str0ng!pass", "12345678901234", "New Company", "new desc", 2);
        ShopkeeperModel shopkeeper = new ShopkeeperModel();
        shopkeeper.setId(1);
        shopkeeper.setName("Updated");
        shopkeeper.setCompanyName("New Company");

        when(service.update(eq("1"), any(UpdateShopkeeperDTO.class))).thenReturn(shopkeeper);

        mockMvc.perform(put("/api/shopkeepers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.companyName").value("New Company"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateShopkeeperDTO dto = new UpdateShopkeeperDTO("Updated", null, null, null, null, null);
        when(service.update(eq("999"), any(UpdateShopkeeperDTO.class)))
                .thenThrow(new ResourceNotFoundException("Shopkeeper not found"));

        mockMvc.perform(put("/api/shopkeepers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete("1");

        mockMvc.perform(delete("/api/shopkeepers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Shopkeeper not found")).when(service).delete("999");

        mockMvc.perform(delete("/api/shopkeepers/999"))
                .andExpect(status().isNotFound());
    }
}
