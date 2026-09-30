package com.passaaqui.backend.unit.achievement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passaaqui.backend.infra.exception.GlobalExceptionHandler;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.achievement.controller.AchievementController;
import com.passaaqui.backend.modules.achievement.dto.AchievementResponseDTO;
import com.passaaqui.backend.modules.achievement.dto.CreateAchievementDTO;
import com.passaaqui.backend.modules.achievement.dto.UnlockAchievementRequestDTO;
import com.passaaqui.backend.modules.achievement.dto.UpdateAchievementDTO;
import com.passaaqui.backend.modules.achievement.service.AchievementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
class AchievementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private AchievementController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "1",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN_ROOT"), new SimpleGrantedAuthority("ROLE_TOURIST"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateAchievementDTO dto = new CreateAchievementDTO("Tapioca Real", "Desc", 50, 1, "Mercado São José", 5);
        MockMultipartFile dataFile = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(dto));
        MockMultipartFile photo = new MockMultipartFile("photo", "img.jpg", "image/jpeg", "content".getBytes());

        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", false, null
        );

        when(achievementService.create(any(CreateAchievementDTO.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/achievements")
                        .file(dataFile)
                        .file(photo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.achievement_id").value(1))
                .andExpect(jsonPath("$.name").value("Tapioca Real"))
                .andExpect(jsonPath("$.category").value("COLHEITA"))
                .andExpect(jsonPath("$.location").value("Mercado São José"))
                .andExpect(jsonPath("$.poi_name").value("Mercado São José"))
                .andExpect(jsonPath("$.unlocked").value(false));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateAchievementDTO dto = new UpdateAchievementDTO("Tapioca Real Atualizada", "Desc 2", 100, 1, "Recife Antigo", 5);
        MockMultipartFile dataFile = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(dto));

        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real Atualizada", "Desc 2", "http://storage/img.jpg", 100, 1, "Gastronomia", "COLHEITA", "Recife Antigo", 5, "Recife Antigo", false, null
        );

        when(achievementService.update(eq(1), any(UpdateAchievementDTO.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/achievements/1")
                        .file(dataFile)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tapioca Real Atualizada"))
                .andExpect(jsonPath("$.category").value("COLHEITA"))
                .andExpect(jsonPath("$.location").value("Recife Antigo"))
                .andExpect(jsonPath("$.xp_reward").value(100));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(achievementService).delete(1);

        mockMvc.perform(delete("/api/achievements/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", true, LocalDateTime.now()
        );

        when(achievementService.getById(1, 1)).thenReturn(response);

        mockMvc.perform(get("/api/achievements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievement_id").value(1))
                .andExpect(jsonPath("$.category").value("COLHEITA"))
                .andExpect(jsonPath("$.location").value("Mercado São José"))
                .andExpect(jsonPath("$.unlocked").value(true));
    }

    @Test
    void listAll_shouldReturn200WithoutFilter() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", false, null
        );

        when(achievementService.listAll(eq(1), isNull(), isNull())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievement_id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tapioca Real"))
                .andExpect(jsonPath("$[0].category").value("COLHEITA"))
                .andExpect(jsonPath("$[0].location").value("Mercado São José"));
    }

    @Test
    void listAll_shouldReturn200WithTudo() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", false, null
        );

        when(achievementService.listAll(eq(1), isNull(), eq("TUDO"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/achievements?category=TUDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievement_id").value(1))
                .andExpect(jsonPath("$[0].category").value("COLHEITA"));
    }

    @Test
    void listAll_shouldReturn200WithValidCategory() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", false, null
        );

        when(achievementService.listAll(eq(1), isNull(), eq("COLHEITA"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/achievements?category=COLHEITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievement_id").value(1))
                .andExpect(jsonPath("$[0].category").value("COLHEITA"));
    }

    @Test
    void listAll_shouldReturnEmptyList_whenCategoryHasNoAchievements() throws Exception {
        when(achievementService.listAll(eq(1), isNull(), eq("FLORACAO"))).thenReturn(List.of());

        mockMvc.perform(get("/api/achievements?category=FLORACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listAll_shouldReturn400_whenInvalidCategory() throws Exception {
        when(achievementService.listAll(eq(1), isNull(), eq("INVALID_CATEGORY")))
                .thenThrow(new com.passaaqui.backend.infra.exception.InvalidRequestException("Invalid achievement category: INVALID_CATEGORY"));

        mockMvc.perform(get("/api/achievements?category=INVALID_CATEGORY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid achievement category: INVALID_CATEGORY"));
    }

    @Test
    void getCategories_shouldReturn200WithCategoriesList() throws Exception {
        var cat1 = new com.passaaqui.backend.modules.achievement.dto.AchievementCategoryDTO("TUDO", "Tudo", "Conquistas gerais");
        var cat2 = new com.passaaqui.backend.modules.achievement.dto.AchievementCategoryDTO("COLHEITA", "Colheita", "Mercados");

        when(achievementService.getCategories()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/achievements/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("TUDO"))
                .andExpect(jsonPath("$[0].label").value("Tudo"))
                .andExpect(jsonPath("$[1].value").value("COLHEITA"));
    }

    @Test
    void getMyAchievements_shouldReturn200() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", true, LocalDateTime.now()
        );

        when(achievementService.listUserUnlockedAchievements(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/achievements/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievement_id").value(1))
                .andExpect(jsonPath("$[0].location").value("Mercado São José"))
                .andExpect(jsonPath("$[0].unlocked").value(true));
    }

    @Test
    void unlock_shouldReturn200() throws Exception {
        AchievementResponseDTO response = new AchievementResponseDTO(
                1, "Tapioca Real", "Desc", "http://storage/img.jpg", 50, 1, "Gastronomia", "COLHEITA", "Mercado São José", 5, "Mercado São José", true, LocalDateTime.now()
        );

        UnlockAchievementRequestDTO unlockDto = new UnlockAchievementRequestDTO(1, "Mercado São José", 5);

        when(achievementService.unlock(1, 1, "Mercado São José", 5)).thenReturn(response);

        mockMvc.perform(post("/api/achievements/1/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unlockDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Mercado São José"))
                .andExpect(jsonPath("$.unlocked").value(true));
    }
}
