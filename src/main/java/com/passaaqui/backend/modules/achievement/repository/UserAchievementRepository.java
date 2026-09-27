package com.passaaqui.backend.modules.achievement.repository;

import com.passaaqui.backend.modules.achievement.model.UserAchievementModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievementModel, Integer> {

    List<UserAchievementModel> findByUserId(Integer userId);

    Optional<UserAchievementModel> findByUserIdAndAchievementId(Integer userId, Integer achievementId);

    boolean existsByUserIdAndAchievementId(Integer userId, Integer achievementId);

    void deleteByAchievementId(Integer achievementId);
}
