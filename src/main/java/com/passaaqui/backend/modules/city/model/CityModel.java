package com.passaaqui.backend.modules.city.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CityModel {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    private String state;

    private String ibgeCode;

    private String region;

    private String microRegion;

    private String mesoRegion;

    private Double minLatitude;

    private Double maxLatitude;

    private Double minLongitude;

    private Double maxLongitude;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "region_code")
    private Long regionCode;

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
