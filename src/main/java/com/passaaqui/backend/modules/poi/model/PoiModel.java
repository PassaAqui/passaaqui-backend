package com.passaaqui.backend.modules.poi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PoiModel {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private Integer xpReward;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "poi_type", nullable = false, columnDefinition = "SMALLINT")
    private PoiType type;

    private Double latitude;

    private Double longitude;

    private Double minLatitude;

    private Double maxLatitude;

    private Double minLongitude;

    private Double maxLongitude;

    private Double averageRating;

    private Integer ratingsCount;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CityModel city;

    @ManyToOne
    private ShopkeeperModel shopkeeper;

    private String image;

    @Transient
    @JsonProperty("image")
    private String imageUrl;

    @JsonIgnore
    public String getImage() {
        return image;
    }

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
