package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.category.controller.CategoryController;
import com.passaaqui.backend.modules.category.dto.CategoryFeedDTO;
import com.passaaqui.backend.modules.category.dto.CreateCategoryDTO;
import com.passaaqui.backend.modules.category.dto.UpdateCategoryDTO;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.service.CategoryService;
import com.passaaqui.backend.modules.product.model.ProductModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService service;

    @InjectMocks
    private CategoryController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void create_shouldReturn200() throws Exception {
        CreateCategoryDTO dto = new CreateCategoryDTO("Food", "Restaurants", 1.0);
        CategoryModel category = new CategoryModel();
        category.setId(1);
        category.setName("Food");
        category.setDescription("Restaurants");

        when(service.create(any(CreateCategoryDTO.class))).thenReturn(category);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void create_shouldReturn400WhenInvalid() throws Exception {
        CreateCategoryDTO dto = new CreateCategoryDTO("", "", null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        CategoryModel cat1 = new CategoryModel();
        cat1.setId(1);
        cat1.setName("Food");
        CategoryModel cat2 = new CategoryModel();
        cat2.setId(2);
        cat2.setName("Tech");

        when(service.findAll()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void findAll_shouldReturn200EmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findById_shouldReturnFeedWithProducts() throws Exception {
        CategoryModel category = new CategoryModel();
        category.setId(1);
        category.setName("Food");

        Page<ProductModel> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(service.findFeedById(eq(1), any())).thenReturn(CategoryFeedDTO.from(category, emptyPage));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.products.content").isArray())
                .andExpect(jsonPath("$.products.totalElements").value(0));
    }

    @Test
    void findById_shouldReturnFeedWithPageParams() throws Exception {
        CategoryModel category = new CategoryModel();
        category.setId(1);
        category.setName("Food");

        Page<ProductModel> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

        when(service.findFeedById(eq(1), any())).thenReturn(CategoryFeedDTO.from(category, page));

        mockMvc.perform(get("/api/categories/1")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.products.size").value(5))
                .andExpect(jsonPath("$.products.number").value(0));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(service.findFeedById(eq(999), any())).thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateCategoryDTO dto = new UpdateCategoryDTO("Updated", "Updated desc", 2.0);
        CategoryModel category = new CategoryModel();
        category.setId(1);
        category.setName("Updated");
        category.setDescription("Updated desc");

        when(service.update(eq(1), any(UpdateCategoryDTO.class))).thenReturn(category);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdateCategoryDTO dto = new UpdateCategoryDTO("Updated", null, null);
        when(service.update(eq(999), any(UpdateCategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete(1);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found")).when(service).delete(999);

        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound());
    }
}
