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
    private final com.passaaqui.backend.modules.poi.repository.PoiRepository poiRepository;
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
        achievement.setLocation(dto.location());

        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            achievement.setCategory(category);
        }

        if (dto.category() != null && !dto.category().isBlank()) {
            achievement.setAchievementCategory(parseCategory(dto.category()));
        }

        if (dto.poiId() != null) {
            com.passaaqui.backend.modules.poi.model.PoiModel poi = poiRepository.findById(dto.poiId())
                    .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
            achievement.setPoi(poi);
        }

        if (photo != null && !photo.isEmpty()) {
            String fileName = storageService.uploadFile(photo, "achievements");
            achievement.setImage(fileName);
        }

        achievement = achievementRepository.save(achievement);

        return toResponseDTO(achievement, null);
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

        if (dto.location() != null) {
            achievement.setLocation(dto.location());
        }

        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            achievement.setCategory(category);
        }

        if (dto.category() != null && !dto.category().isBlank()) {
            achievement.setAchievementCategory(parseCategory(dto.category()));
        }

        if (dto.poiId() != null) {
            com.passaaqui.backend.modules.poi.model.PoiModel poi = poiRepository.findById(dto.poiId())
                    .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
            achievement.setPoi(poi);
        }

        if (photo != null && !photo.isEmpty()) {
            if (achievement.getImage() != null) {
                storageService.deleteFile(achievement.getImage());
            }
            String fileName = storageService.uploadFile(photo, "achievements");
            achievement.setImage(fileName);
        }

        achievement = achievementRepository.save(achievement);

        return toResponseDTO(achievement, null);
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
            return toResponseDTO(achievement, userAchievement.orElse(null));
        }

        return toResponseDTO(achievement, null);
    }

    public List<com.passaaqui.backend.modules.achievement.dto.AchievementCategoryDTO> getCategories() {
        return java.util.Arrays.stream(com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory.values())
                .map(cat -> new com.passaaqui.backend.modules.achievement.dto.AchievementCategoryDTO(
                        cat.name(),
                        cat.getLabel(),
                        cat.getDescription()
                ))
                .toList();
    }

    public List<AchievementResponseDTO> listAll(Integer userId, Integer categoryId, String categoryFilter) {
        List<AchievementModel> achievements;

        if (categoryFilter != null && !categoryFilter.isBlank()) {
            com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory parsed = parseCategory(categoryFilter);
            if (parsed == com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory.TUDO) {
                achievements = categoryId != null ? achievementRepository.findByCategoryId(categoryId) : achievementRepository.findAll();
            } else {
                achievements = achievementRepository.findByAchievementCategory(parsed);
                if (categoryId != null) {
                    achievements = achievements.stream()
                            .filter(a -> a.getCategory() != null && categoryId.equals(a.getCategory().getId()))
                            .toList();
                }
            }
        } else if (categoryId != null) {
            achievements = achievementRepository.findByCategoryId(categoryId);
        } else {
            achievements = achievementRepository.findAll();
        }

        if (userId != null) {
            Map<Integer, UserAchievementModel> userMap = userAchievementRepository.findByUserId(userId)
                    .stream()
                    .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua, (a, b) -> a));

            return achievements.stream()
                    .map(achievement -> toResponseDTO(achievement, userMap.get(achievement.getId())))
                    .toList();
        }

        return achievements.stream()
                .map(achievement -> toResponseDTO(achievement, null))
                .toList();
    }

    private com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory parseCategory(String value) {
        try {
            return com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new com.passaaqui.backend.infra.exception.InvalidRequestException("Invalid achievement category: " + value);
        }
    }

    public List<AchievementResponseDTO> listUserUnlockedAchievements(Integer userId) {
        if (!touristRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Tourist not found");
        }

        return userAchievementRepository.findByUserId(userId)
                .stream()
                .map(ua -> toResponseDTO(ua.getAchievement(), ua))
                .toList();
    }

    @Transactional
    public AchievementResponseDTO unlock(Integer achievementId, Integer targetUserId, String location, Integer poiId) {
        AchievementModel achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        TouristModel tourist = touristRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        if (userAchievementRepository.existsByUserIdAndAchievementId(targetUserId, achievementId)) {
            throw new ConflictException("Achievement already unlocked by this user");
        }

        com.passaaqui.backend.modules.poi.model.PoiModel poi = null;
        if (poiId != null) {
            poi = poiRepository.findById(poiId)
                    .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
        } else if (achievement.getPoi() != null) {
            poi = achievement.getPoi();
        }

        String effectiveLocation = location != null && !location.isBlank() ? location : achievement.getLocation();
        if (effectiveLocation == null && poi != null) {
            effectiveLocation = poi.getName();
        }

        UserAchievementModel userAchievement = UserAchievementModel.builder()
                .user(tourist)
                .achievement(achievement)
                .location(effectiveLocation)
                .poi(poi)
                .build();

        userAchievement = userAchievementRepository.save(userAchievement);

        if (achievement.getXpReward() != null && achievement.getXpReward() > 0) {
            int currentXp = tourist.getCurrentXP() != null ? tourist.getCurrentXP() : 0;
            tourist.setCurrentXP(currentXp + achievement.getXpReward());
            touristRepository.save(tourist);
        }

        return toResponseDTO(achievement, userAchievement);
    }

    private AchievementResponseDTO toResponseDTO(AchievementModel achievement, UserAchievementModel userAchievement) {
        String photoUrl = null;
        if (achievement.getImage() != null && !achievement.getImage().isBlank()) {
            photoUrl = storageService.getFileUrl(achievement.getImage());
        }

        boolean unlocked = userAchievement != null;
        java.time.LocalDateTime unlockedAt = userAchievement != null ? userAchievement.getUnlockedAt() : null;

        String location = null;
        Integer poiId = null;
        String poiName = null;

        if (userAchievement != null) {
            location = userAchievement.getLocation();
            if (userAchievement.getPoi() != null) {
                poiId = userAchievement.getPoi().getId();
                poiName = userAchievement.getPoi().getName();
            }
        }

        if (location == null) {
            location = achievement.getLocation();
        }
        if (poiId == null && achievement.getPoi() != null) {
            poiId = achievement.getPoi().getId();
            poiName = achievement.getPoi().getName();
        }

        return new AchievementResponseDTO(
                achievement.getId(),
                achievement.getName(),
                achievement.getDescription(),
                photoUrl,
                achievement.getXpReward(),
                achievement.getCategory() != null ? achievement.getCategory().getId() : null,
                achievement.getCategory() != null ? achievement.getCategory().getName() : null,
                achievement.getAchievementCategory() != null ? achievement.getAchievementCategory().name() : null,
                location,
                poiId,
                poiName,
                unlocked,
                unlockedAt
        );
    }

    public List<AchievementResponseDTO> listAll(Integer userId, Integer categoryId) {
        return listAll(userId, categoryId, null);
    }
}
