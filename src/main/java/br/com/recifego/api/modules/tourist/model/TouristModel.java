package br.com.recifego.api.modules.tourist.model;

import org.springframework.data.geo.Point;

import br.com.recifego.api.modules.user.model.UserModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TouristModel extends UserModel {

    private String deviceId;
    private Point lastKnownLocation;
    private Integer currentXP, level;
    
}
