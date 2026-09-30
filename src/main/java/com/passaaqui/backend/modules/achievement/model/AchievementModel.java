package com.passaaqui.backend.modules.achievement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_achievements")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AchievementModel {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_name")
    private String image;

    @Transient
    private String imageUrl;

    private Integer xpReward;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private CategoryModel category;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_category", length = 50)
    private com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory achievementCategory = com.passaaqui.backend.modules.achievement.model.enums.AchievementCategory.TUDO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poi_id")
    private com.passaaqui.backend.modules.poi.model.PoiModel poi;

    @Column(name = "location_name")
    private String location;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @JsonIgnore
    public String getImage() {
        return image;
    }
}
