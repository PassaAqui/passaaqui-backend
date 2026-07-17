package com.passaaqui.backend.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.poi.controller.PoiController;
import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.PoiDetailDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.service.PoiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PoiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PoiService service;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private PoiController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void create_shouldReturn200() throws Exception {
        CreatePoiDTO dto = new CreatePoiDTO("Test POI", "A nice place", 50, PoiType.TOURIST_POINT, -23.5, -46.6, null, null, null, null, 1);
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Test POI");
        poi.setDescription("A nice place");

        when(service.create(any(CreatePoiDTO.class), any())).thenReturn(poi);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/pois")
                        .file(dataPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test POI"));
    }

    @Test
    void create_shouldReturn200WithImage() throws Exception {
        CreatePoiDTO dto = new CreatePoiDTO("Test POI", "A nice place", 50, PoiType.TOURIST_POINT, -23.5, -46.6, null, null, null, null, 1);
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Test POI");
        poi.setImage("poi-img.jpg");

        when(service.create(any(CreatePoiDTO.class), any())).thenReturn(poi);
        when(storageService.getFileUrl("poi-img.jpg")).thenReturn("http://storage.com/poi-img.jpg");

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));
        MockMultipartFile imagePart = new MockMultipartFile("image", "img.jpg", "image/jpeg", "img-data".getBytes());

        mockMvc.perform(multipart("/api/pois")
                        .file(dataPart)
                        .file(imagePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/poi-img.jpg"));
    }

    @Test
    void create_shouldReturn400WhenInvalid() throws Exception {
        CreatePoiDTO dto = new CreatePoiDTO("", null, null, PoiType.TOURIST_POINT, null, null, null, null, null, null, null);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/pois")
                        .file(dataPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdatePoiDTO dto = new UpdatePoiDTO("Updated POI", "Updated desc", 100, -23.5, -46.6, null, null, null, null, 1);
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Updated POI");

        when(service.update(eq(1), any(UpdatePoiDTO.class))).thenReturn(poi);

        mockMvc.perform(put("/api/pois/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated POI"));
    }

    @Test
    void update_shouldReturn200WithImageUrl() throws Exception {
        UpdatePoiDTO dto = new UpdatePoiDTO("Updated POI", null, null, null, null, null, null, null, null, null);
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Updated POI");
        poi.setImage("img.jpg");

        when(service.update(eq(1), any(UpdatePoiDTO.class))).thenReturn(poi);
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(put("/api/pois/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/img.jpg"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        UpdatePoiDTO dto = new UpdatePoiDTO("Updated", null, null, null, null, null, null, null, null, null);
        when(service.update(eq(999), any(UpdatePoiDTO.class)))
                .thenThrow(new ResourceNotFoundException("POI not found"));

        mockMvc.perform(put("/api/pois/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete(1);

        mockMvc.perform(delete("/api/pois/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("POI not found")).when(service).delete(999);

        mockMvc.perform(delete("/api/pois/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Test POI");

        Page<PoiModel> page = new PageImpl<>(List.of(poi), PageRequest.of(0, 20), 1);
        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/pois")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test POI"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findAll_shouldReturn200WithImageUrls() throws Exception {
        PoiModel poi = new PoiModel();
        poi.setId(1);
        poi.setName("Test POI");
        poi.setImage("img.jpg");

        Page<PoiModel> page = new PageImpl<>(List.of(poi), PageRequest.of(0, 20), 1);
        when(service.findAll(any(Pageable.class))).thenReturn(page);
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(get("/api/pois"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].image").value("http://storage.com/img.jpg"));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        PoiDetailDTO dto = new PoiDetailDTO(1, "Test POI", "desc", 50, PoiType.TOURIST_POINT,
                -23.5, -46.6, 4.5, 10, null, List.of());

        when(service.findDetailById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/pois/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test POI"));
    }

    @Test
    void findById_shouldReturn200WithImageUrl() throws Exception {
        PoiDetailDTO dto = new PoiDetailDTO(1, "Test POI", "desc", 50, PoiType.TOURIST_POINT,
                -23.5, -46.6, 4.5, 10, "http://storage.com/img.jpg", List.of());

        when(service.findDetailById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/pois/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/img.jpg"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(service.findDetailById(999)).thenThrow(new ResourceNotFoundException("POI not found"));

        mockMvc.perform(get("/api/pois/999"))
                .andExpect(status().isNotFound());
    }
}
