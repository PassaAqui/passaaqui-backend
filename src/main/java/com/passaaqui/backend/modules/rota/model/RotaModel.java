package com.passaaqui.backend.modules.rota.model;

import com.passaaqui.backend.modules.tourist.model.TouristModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_rotas")
@Getter
@Setter
@NoArgsConstructor
public class RotaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false)
    private TouristModel tourist;

    @Column(nullable = false)
    private Double startLatitude;

    @Column(nullable = false)
    private Double startLongitude;

    @Column(nullable = false)
    private Double endLatitude;

    @Column(nullable = false)
    private Double endLongitude;

    private String mode;

    private Integer poiId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;
}
