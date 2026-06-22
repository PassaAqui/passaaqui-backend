package com.passaaqui.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.city.controller.CityController;
import com.passaaqui.backend.modules.city.dto.CreateCityDTO;
import com.passaaqui.backend.modules.city.dto.UpdateCityDTO;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.service.CityService;
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
class CityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CityService cityService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private CityController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createCity_shouldReturn200() throws Exception {
        CreateCityDTO dto = new CreateCityDTO("1234567", "Test City", -10.0, -5.0, -40.0, -35.0);
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");
        city.setIbgeCode("1234567");

        when(cityService.createCity(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(city);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/city/create")
                        .file(dataPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test City"));
    }

    @Test
    void createCity_shouldReturn200WithImage() throws Exception {
        CreateCityDTO dto = new CreateCityDTO("1234567", "Test City", -10.0, -5.0, -40.0, -35.0);
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");
        city.setImage("city-img.jpg");

        when(cityService.createCity(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(city);
        when(storageService.getFileUrl("city-img.jpg")).thenReturn("http://storage.com/city-img.jpg");

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));
        MockMultipartFile imagePart = new MockMultipartFile("image", "img.jpg", "image/jpeg", "img-data".getBytes());

        mockMvc.perform(multipart("/api/city/create")
                        .file(dataPart)
                        .file(imagePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/city-img.jpg"));
    }

    @Test
    void createCity_shouldReturn400WhenInvalid() throws Exception {
        CreateCityDTO dto = new CreateCityDTO("", "", null, null, null, null);

        MockMultipartFile dataPart = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        mockMvc.perform(multipart("/api/city/create")
                        .file(dataPart))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCity_shouldReturn200() throws Exception {
        UpdateCityDTO dto = new UpdateCityDTO("Updated City", "desc", "SP", "1234567", "Southeast", "Micro", "Meso", "São Paulo", 35L, -10.0, -5.0, -40.0, -35.0);
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Updated City");

        when(cityService.updateCity(eq(1), any(UpdateCityDTO.class))).thenReturn(city);

        mockMvc.perform(put("/api/city/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated City"));
    }

    @Test
    void updateCity_shouldReturn200WithImageUrl() throws Exception {
        UpdateCityDTO dto = new UpdateCityDTO("Updated City", "desc", "SP", "1234567", "Southeast", "Micro", "Meso", "São Paulo", 35L, -10.0, -5.0, -40.0, -35.0);
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Updated City");
        city.setImage("img.jpg");

        when(cityService.updateCity(eq(1), any(UpdateCityDTO.class))).thenReturn(city);
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(put("/api/city/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/img.jpg"));
    }

    @Test
    void updateCity_shouldReturn400WhenInvalid() throws Exception {
        UpdateCityDTO dto = new UpdateCityDTO("", "", "", "", "", "", "", "", null, null, null, null, null);

        mockMvc.perform(put("/api/city/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCity_shouldReturn404WhenNotFound() throws Exception {
        UpdateCityDTO dto = new UpdateCityDTO("Updated", "desc", "SP", "1234567", "Southeast", "Micro", "Meso", "SP", 35L, null, null, null, null);
        when(cityService.updateCity(eq(999), any(UpdateCityDTO.class)))
                .thenThrow(new ResourceNotFoundException("City not found"));

        mockMvc.perform(put("/api/city/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCity_shouldReturn204() throws Exception {
        doNothing().when(cityService).deleteCity(1);

        mockMvc.perform(delete("/api/city/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCity_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("City not found")).when(cityService).deleteCity(999);

        mockMvc.perform(delete("/api/city/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");

        when(cityService.getCityById(1)).thenReturn(city);

        mockMvc.perform(get("/api/city/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test City"));
    }

    @Test
    void getById_shouldReturn200WithImageUrl() throws Exception {
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");
        city.setImage("img.jpg");

        when(cityService.getCityById(1)).thenReturn(city);
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(get("/api/city/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("http://storage.com/img.jpg"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(cityService.getCityById(999)).thenThrow(new ResourceNotFoundException("City not found"));

        mockMvc.perform(get("/api/city/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");

        CityModel city2 = new CityModel();
        city2.setId(2);
        city2.setName("Test City 2");

        when(cityService.getAllCities()).thenReturn(List.of(city, city2));

        mockMvc.perform(get("/api/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAll_shouldReturn200WithImageUrls() throws Exception {
        CityModel city = new CityModel();
        city.setId(1);
        city.setName("Test City");
        city.setImage("img.jpg");

        when(cityService.getAllCities()).thenReturn(List.of(city));
        when(storageService.getFileUrl("img.jpg")).thenReturn("http://storage.com/img.jpg");

        mockMvc.perform(get("/api/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].image").value("http://storage.com/img.jpg"));
    }

    @Test
    void getAll_shouldReturn200EmptyList() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of());

        mockMvc.perform(get("/api/city"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
