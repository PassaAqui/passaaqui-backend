package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.product.controller.ProductController;
import com.passaaqui.backend.modules.product.dto.CreateProductDTO;
import com.passaaqui.backend.modules.product.dto.UpdateProductDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.service.ProductService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService service;

    @InjectMocks
    private ProductController controller;

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
        CreateProductDTO dto = new CreateProductDTO("Product", "Description", 29.99, 100, 50, 1, 1, 2, true, false);
        ProductModel product = new ProductModel();
        product.setId(1);
        product.setName("Product");
        product.setPrice(29.99);

        when(service.create(any(CreateProductDTO.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product"));
    }

    @Test
    void create_shouldReturn400WhenInvalid() throws Exception {
        CreateProductDTO dto = new CreateProductDTO("", "", null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn404WhenShopkeeperNotFound() throws Exception {
        CreateProductDTO dto = new CreateProductDTO("Product", "Desc", 10.0, null, null, 999, 1, 1, null, null);
        when(service.create(any(CreateProductDTO.class)))
                .thenThrow(new ResourceNotFoundException("Shopkeeper not found"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateProductDTO dto = new UpdateProductDTO("Updated", "Updated desc", 39.99, 200, null, 1, 1, 2, null, null);
        ProductModel product = new ProductModel();
        product.setId(1);
        product.setName("Updated");
        product.setPrice(39.99);

        when(service.update(eq(1), any(UpdateProductDTO.class))).thenReturn(product);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.price").value(39.99));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateProductDTO dto = new UpdateProductDTO("Updated", null, null, null, null, null, null, null, null, null);
        when(service.update(eq(999), any(UpdateProductDTO.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete(1);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found")).when(service).delete(999);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        ProductModel p1 = new ProductModel();
        p1.setId(1);
        p1.setName("Product 1");
        ProductModel p2 = new ProductModel();
        p2.setId(2);
        p2.setName("Product 2");

        when(service.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void findAll_shouldReturn200EmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRecentProducts_shouldReturn200() throws Exception {
        ProductModel p1 = new ProductModel();
        p1.setId(1);
        p1.setName("Recent Product");

        when(service.getRecentProducts()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/products/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getRecentProducts_shouldReturn200EmptyList() throws Exception {
        when(service.getRecentProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        ProductModel product = new ProductModel();
        product.setId(1);
        product.setName("Product");

        when(service.findById(1)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(service.findById(999)).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByIdWithAccessCheck_shouldReturn200() throws Exception {
        ProductModel product = new ProductModel();
        product.setId(1);
        product.setName("Protected Product");

        when(service.findByIdWithAccessCheck(1)).thenReturn(product);

        mockMvc.perform(get("/api/products/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Protected Product"));
    }

    @Test
    void findByIdWithAccessCheck_shouldReturn404WhenNotFound() throws Exception {
        when(service.findByIdWithAccessCheck(999))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/999/details"))
                .andExpect(status().isNotFound());
    }
}
