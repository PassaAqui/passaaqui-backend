package com.passaaqui.backend.modules.achievement.model;

import com.passaaqui.backend.modules.tourist.model.TouristModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user_achievements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserAchievementModel {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private TouristModel user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private AchievementModel achievement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poi_id")
    private com.passaaqui.backend.modules.poi.model.PoiModel poi;

    @Column(name = "location_name")
    private String location;

    @CreatedDate
    @Column(name = "unlocked_at", updatable = false)
    private LocalDateTime unlockedAt;
}
