package com.passaaqui.backend.modules.poi.model;

import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "poi_visit_model")
public class PoiVisitModel {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PoiModel poi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserModel user;

    private Integer xpEarned;

    private Double distanceKm;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime visitedAt;
}
