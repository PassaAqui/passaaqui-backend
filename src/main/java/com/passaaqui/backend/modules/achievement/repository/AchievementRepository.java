package com.passaaqui.backend.modules.achievement.repository;

import com.passaaqui.backend.modules.achievement.model.AchievementModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<AchievementModel, Integer> {

    Optional<AchievementModel> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<AchievementModel> findByCategoryId(Integer categoryId);

    List<AchievementModel> findByAchievementCategory(com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory achievementCategory);
}
