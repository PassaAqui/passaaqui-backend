package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.product.controller.ProductRatingController;
import com.passaaqui.backend.modules.product.dto.ProductRatingResponseDTO;
import com.passaaqui.backend.modules.product.service.ProductRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductRatingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductRatingService ratingService;

    @InjectMocks
    private ProductRatingController controller;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("1", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rate_shouldReturn200_whenValidMultipartRequest() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", "image/jpeg", "image".getBytes());
        ProductRatingResponseDTO response = new ProductRatingResponseDTO(
                1,
                10,
                "Tapioca Clássica",
                "#A3F92",
                5,
                "Muito bom",
                List.of("http://minio/photo.jpg"),
                null,
                LocalDateTime.now()
        );

        when(ratingService.rate(eq(10), eq(1), eq(5), eq("Muito bom"), eq("#A3F92"), any(), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/products/10/ratings")
                        .file(photo)
                        .param("rating", "5")
                        .param("comment", "Muito bom")
                        .param("order_id", "#A3F92"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product_id").value(10))
                .andExpect(jsonPath("$.product_name").value("Tapioca Clássica"))
                .andExpect(jsonPath("$.order_id").value("#A3F92"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Muito bom"))
                .andExpect(jsonPath("$.photos[0]").value("http://minio/photo.jpg"));
    }

    @Test
    void rate_shouldReturn400_whenServiceThrowsInvalidRequestException() throws Exception {
        when(ratingService.rate(eq(10), eq(1), eq(5), isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new InvalidRequestException("At least one image or video is required"));

        mockMvc.perform(multipart("/api/products/10/ratings")
                        .param("rating", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one image or video is required"));
    }

    @Test
    void rate_shouldReturn404_whenProductNotFound() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", "image/jpeg", "image".getBytes());
        when(ratingService.rate(eq(999), eq(1), eq(5), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(multipart("/api/products/999/ratings")
                        .file(photo)
                        .param("rating", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatings_shouldReturn200() throws Exception {
        ProductRatingResponseDTO rating1 = new ProductRatingResponseDTO(
                1, 10, "Tapioca", "#A3F92", 5, "Ótimo", List.of("http://minio/1.jpg"), null, LocalDateTime.now()
        );

        when(ratingService.getRatingsByProductId(10)).thenReturn(List.of(rating1));

        mockMvc.perform(get("/api/products/10/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].product_id").value(10))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].photos[0]").value("http://minio/1.jpg"));
    }
}
