package com.passaaqui.backend.modules.tourist.model;

import com.passaaqui.backend.modules.user.model.UserModel;
import org.springframework.data.geo.Point;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TouristModel extends UserModel {

    private String deviceId, documentId;
    private Point lastKnownLocation;
    private Integer currentXP, level;
    
}
