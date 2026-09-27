package com.passaaqui.backend.modules.achievement.service;

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
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final TouristRepository touristRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    @Transactional
    public AchievementResponseDTO create(CreateAchievementDTO dto, MultipartFile photo) {
        if (achievementRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ConflictException("Achievement with this name already exists");
        }

        AchievementModel achievement = new AchievementModel();
        achievement.setName(dto.name());
        achievement.setDescription(dto.description());
        achievement.setXpReward(dto.xpReward());

        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            achievement.setCategory(category);
        }

        if (photo != null && !photo.isEmpty()) {
            String fileName = storageService.uploadFile(photo, "achievements");
            achievement.setImage(fileName);
        }

        achievement = achievementRepository.save(achievement);

        return toResponseDTO(achievement, false, null);
    }

    @Transactional
    public AchievementResponseDTO update(Integer id, UpdateAchievementDTO dto, MultipartFile photo) {
        AchievementModel achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        if (dto.name() != null && !dto.name().isBlank()) {
            achievementRepository.findByNameIgnoreCase(dto.name()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ConflictException("Achievement with this name already exists");
                }
            });
            achievement.setName(dto.name());
        }

        if (dto.description() != null) {
            achievement.setDescription(dto.description());
        }

        if (dto.xpReward() != null) {
            achievement.setXpReward(dto.xpReward());
        }

        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            achievement.setCategory(category);
        }

        if (photo != null && !photo.isEmpty()) {
            if (achievement.getImage() != null) {
                storageService.deleteFile(achievement.getImage());
            }
            String fileName = storageService.uploadFile(photo, "achievements");
            achievement.setImage(fileName);
        }

        achievement = achievementRepository.save(achievement);

        return toResponseDTO(achievement, false, null);
    }

    @Transactional
    public void delete(Integer id) {
        AchievementModel achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        if (achievement.getImage() != null) {
            storageService.deleteFile(achievement.getImage());
        }

        userAchievementRepository.deleteByAchievementId(id);
        achievementRepository.delete(achievement);
    }

    public AchievementResponseDTO getById(Integer id, Integer userId) {
        AchievementModel achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        if (userId != null) {
            var userAchievement = userAchievementRepository.findByUserIdAndAchievementId(userId, id);
            boolean unlocked = userAchievement.isPresent();
            var unlockedAt = userAchievement.map(UserAchievementModel::getUnlockedAt).orElse(null);
            return toResponseDTO(achievement, unlocked, unlockedAt);
        }

        return toResponseDTO(achievement, false, null);
    }

    public List<AchievementResponseDTO> listAll(Integer userId, Integer categoryId) {
        List<AchievementModel> achievements;
        if (categoryId != null) {
            achievements = achievementRepository.findByCategoryId(categoryId);
        } else {
            achievements = achievementRepository.findAll();
        }

        if (userId != null) {
            Map<Integer, UserAchievementModel> userMap = userAchievementRepository.findByUserId(userId)
                    .stream()
                    .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua, (a, b) -> a));

            return achievements.stream()
                    .map(achievement -> {
                        UserAchievementModel ua = userMap.get(achievement.getId());
                        boolean unlocked = ua != null;
                        var unlockedAt = ua != null ? ua.getUnlockedAt() : null;
                        return toResponseDTO(achievement, unlocked, unlockedAt);
                    })
                    .toList();
        }

        return achievements.stream()
                .map(achievement -> toResponseDTO(achievement, false, null))
                .toList();
    }

    public List<AchievementResponseDTO> listUserUnlockedAchievements(Integer userId) {
        if (!touristRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Tourist not found");
        }

        return userAchievementRepository.findByUserId(userId)
                .stream()
                .map(ua -> toResponseDTO(ua.getAchievement(), true, ua.getUnlockedAt()))
                .toList();
    }

    @Transactional
    public AchievementResponseDTO unlock(Integer achievementId, Integer targetUserId) {
        AchievementModel achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        TouristModel tourist = touristRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        if (userAchievementRepository.existsByUserIdAndAchievementId(targetUserId, achievementId)) {
            throw new ConflictException("Achievement already unlocked by this user");
        }

        UserAchievementModel userAchievement = UserAchievementModel.builder()
                .user(tourist)
                .achievement(achievement)
                .build();

        userAchievement = userAchievementRepository.save(userAchievement);

        if (achievement.getXpReward() != null && achievement.getXpReward() > 0) {
            int currentXp = tourist.getCurrentXP() != null ? tourist.getCurrentXP() : 0;
            tourist.setCurrentXP(currentXp + achievement.getXpReward());
            touristRepository.save(tourist);
        }

        return toResponseDTO(achievement, true, userAchievement.getUnlockedAt());
    }

    private AchievementResponseDTO toResponseDTO(AchievementModel achievement, boolean unlocked, java.time.LocalDateTime unlockedAt) {
        String photoUrl = null;
        if (achievement.getImage() != null && !achievement.getImage().isBlank()) {
            photoUrl = storageService.getFileUrl(achievement.getImage());
        }

        return new AchievementResponseDTO(
                achievement.getId(),
                achievement.getName(),
                achievement.getDescription(),
                photoUrl,
                achievement.getXpReward(),
                achievement.getCategory() != null ? achievement.getCategory().getId() : null,
                achievement.getCategory() != null ? achievement.getCategory().getName() : null,
                unlocked,
                unlockedAt
        );
    }
}
