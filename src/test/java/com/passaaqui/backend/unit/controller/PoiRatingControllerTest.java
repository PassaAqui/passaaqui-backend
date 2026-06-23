package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.poi.controller.PoiRatingController;
import com.passaaqui.backend.modules.poi.dto.CreatePoiRatingDTO;
import com.passaaqui.backend.modules.poi.model.PoiRatingModel;
import com.passaaqui.backend.modules.poi.service.PoiRatingService;
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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PoiRatingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PoiRatingService ratingService;

    @InjectMocks
    private PoiRatingController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("1", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void rate_shouldReturn200() throws Exception {
        CreatePoiRatingDTO dto = new CreatePoiRatingDTO(4);
        PoiRatingModel rating = new PoiRatingModel();
        rating.setId(1);
        rating.setRating(4);

        when(ratingService.rate(eq(1), anyInt(), any(CreatePoiRatingDTO.class))).thenReturn(rating);

        mockMvc.perform(post("/api/pois/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void rate_shouldReturn400WhenInvalid() throws Exception {
        CreatePoiRatingDTO dto = new CreatePoiRatingDTO(null);

        mockMvc.perform(post("/api/pois/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rate_shouldReturn400WhenRatingOutOfRange() throws Exception {
        CreatePoiRatingDTO dto = new CreatePoiRatingDTO(10);

        mockMvc.perform(post("/api/pois/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rate_shouldReturn404WhenPoiNotFound() throws Exception {
        CreatePoiRatingDTO dto = new CreatePoiRatingDTO(3);
        when(ratingService.rate(eq(999), anyInt(), any(CreatePoiRatingDTO.class)))
                .thenThrow(new ResourceNotFoundException("POI not found"));

        mockMvc.perform(post("/api/pois/999/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatings_shouldReturn200() throws Exception {
        PoiRatingModel rating1 = new PoiRatingModel();
        rating1.setId(1);
        rating1.setRating(4);
        PoiRatingModel rating2 = new PoiRatingModel();
        rating2.setId(2);
        rating2.setRating(5);

        when(ratingService.getRatingsByPoiId(1)).thenReturn(List.of(rating1, rating2));

        mockMvc.perform(get("/api/pois/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getRatings_shouldReturn200EmptyList() throws Exception {
        when(ratingService.getRatingsByPoiId(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/pois/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getRatings_shouldReturn404WhenPoiNotFound() throws Exception {
        when(ratingService.getRatingsByPoiId(999))
                .thenThrow(new ResourceNotFoundException("POI not found"));

        mockMvc.perform(get("/api/pois/999/ratings"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rate_shouldReturn400WhenRatingBelowMin() throws Exception {
        CreatePoiRatingDTO dto = new CreatePoiRatingDTO(-1);

        mockMvc.perform(post("/api/pois/1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
