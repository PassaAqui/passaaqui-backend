package com.passaaqui.backend.unit.achievement;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.achievement.dto.AchievementResponseDTO;
import com.passaaqui.backend.modules.achievement.dto.CreateAchievementDTO;
import com.passaaqui.backend.modules.achievement.dto.UpdateAchievementDTO;
import com.passaaqui.backend.modules.achievement.model.AchievementModel;
import com.passaaqui.backend.modules.achievement.model.UserAchievementModel;
import com.passaaqui.backend.modules.achievement.repository.AchievementRepository;
import com.passaaqui.backend.modules.achievement.repository.UserAchievementRepository;
import com.passaaqui.backend.modules.achievement.service.AchievementService;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private TouristRepository touristRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AchievementService achievementService;

    private AchievementModel achievement;
    private CategoryModel category;
    private TouristModel tourist;
    private MockMultipartFile photo;

    @BeforeEach
    void setUp() {
        category = new CategoryModel();
        category.setId(1);
        category.setName("Gastronomia");

        achievement = new AchievementModel();
        achievement.setId(1);
        achievement.setName("Tapioca real");
        achievement.setDescription("Colete para colar");
        achievement.setCategory(category);
        achievement.setXpReward(50);
        achievement.setImage("tapioca.jpg");

        tourist = new TouristModel();
        tourist.setId(10);
        tourist.setCurrentXP(100);

        photo = new MockMultipartFile("photo", "test.png", "image/png", "bytes".getBytes());
    }

    @Test
    void create_shouldCreateAchievement_whenValidInput() {
        CreateAchievementDTO dto = new CreateAchievementDTO("Tapioca real", "Colete para colar", 50, 1);

        when(achievementRepository.existsByNameIgnoreCase("Tapioca real")).thenReturn(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(storageService.uploadFile(eq(photo), eq("achievements"))).thenReturn("tapioca.jpg");
        when(achievementRepository.save(any(AchievementModel.class))).thenAnswer(i -> {
            AchievementModel m = i.getArgument(0);
            m.setId(1);
            return m;
        });
        when(storageService.getFileUrl("tapioca.jpg")).thenReturn("http://storage/tapioca.jpg");

        AchievementResponseDTO result = achievementService.create(dto, photo);

        assertNotNull(result);
        assertEquals("Tapioca real", result.name());
        assertEquals("Colete para colar", result.description());
        assertEquals(50, result.xpReward());
        assertEquals("Gastronomia", result.categoryName());
        assertEquals("http://storage/tapioca.jpg", result.photoUrl());
        assertFalse(result.unlocked());
    }

    @Test
    void create_shouldThrowConflict_whenNameAlreadyExists() {
        CreateAchievementDTO dto = new CreateAchievementDTO("Tapioca real", "Colete para colar", 50, 1);
        when(achievementRepository.existsByNameIgnoreCase("Tapioca real")).thenReturn(true);

        assertThrows(ConflictException.class, () -> achievementService.create(dto, photo));
    }

    @Test
    void update_shouldUpdateAchievement_whenValid() {
        UpdateAchievementDTO dto = new UpdateAchievementDTO("Tapioca Real Atualizada", "Nova descrição", 100, 1);

        when(achievementRepository.findById(1)).thenReturn(Optional.of(achievement));
        when(achievementRepository.findByNameIgnoreCase("Tapioca Real Atualizada")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(storageService.uploadFile(eq(photo), eq("achievements"))).thenReturn("nova-foto.jpg");
        when(achievementRepository.save(any(AchievementModel.class))).thenAnswer(i -> i.getArgument(0));
        when(storageService.getFileUrl("nova-foto.jpg")).thenReturn("http://storage/nova-foto.jpg");

        AchievementResponseDTO result = achievementService.update(1, dto, photo);

        assertNotNull(result);
        assertEquals("Tapioca Real Atualizada", result.name());
        assertEquals(100, result.xpReward());
        verify(storageService).deleteFile("tapioca.jpg");
    }

    @Test
    void delete_shouldDeleteAchievementAndRelatedUserAchievements() {
        when(achievementRepository.findById(1)).thenReturn(Optional.of(achievement));

        achievementService.delete(1);

        verify(storageService).deleteFile("tapioca.jpg");
        verify(userAchievementRepository).deleteByAchievementId(1);
        verify(achievementRepository).delete(achievement);
    }

    @Test
    void listAll_shouldReturnAchievementsWithUnlockedStatus_whenUserIdProvided() {
        UserAchievementModel ua = UserAchievementModel.builder()
                .id(1)
                .user(tourist)
                .achievement(achievement)
                .unlockedAt(LocalDateTime.of(2026, 4, 1, 12, 0))
                .build();

        when(achievementRepository.findAll()).thenReturn(List.of(achievement));
        when(userAchievementRepository.findByUserId(10)).thenReturn(List.of(ua));
        when(storageService.getFileUrl("tapioca.jpg")).thenReturn("http://storage/tapioca.jpg");

        List<AchievementResponseDTO> list = achievementService.listAll(10, null);

        assertEquals(1, list.size());
        assertTrue(list.get(0).unlocked());
        assertEquals(LocalDateTime.of(2026, 4, 1, 12, 0), list.get(0).unlockedAt());
    }

    @Test
    void unlock_shouldUnlockAchievementAndAddXpToTourist() {
        when(achievementRepository.findById(1)).thenReturn(Optional.of(achievement));
        when(touristRepository.findById(10)).thenReturn(Optional.of(tourist));
        when(userAchievementRepository.existsByUserIdAndAchievementId(10, 1)).thenReturn(false);
        when(userAchievementRepository.save(any(UserAchievementModel.class))).thenAnswer(i -> {
            UserAchievementModel m = i.getArgument(0);
            m.setUnlockedAt(LocalDateTime.now());
            return m;
        });

        AchievementResponseDTO result = achievementService.unlock(1, 10);

        assertTrue(result.unlocked());
        assertEquals(150, tourist.getCurrentXP());
        verify(touristRepository).save(tourist);
    }

    @Test
    void unlock_shouldThrowConflict_whenAlreadyUnlocked() {
        when(achievementRepository.findById(1)).thenReturn(Optional.of(achievement));
        when(touristRepository.findById(10)).thenReturn(Optional.of(tourist));
        when(userAchievementRepository.existsByUserIdAndAchievementId(10, 1)).thenReturn(true);

        assertThrows(ConflictException.class, () -> achievementService.unlock(1, 10));
    }
}
