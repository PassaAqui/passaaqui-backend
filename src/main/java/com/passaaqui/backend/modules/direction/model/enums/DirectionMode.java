package com.passaaqui.backend.modules.direction.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DirectionMode {

    DRIVING_CAR("driving-car"),
    DRIVING_HGV("driving-hgv"),
    CYCLING_REGULAR("cycling-regular"),
    CYCLING_ROAD("cycling-road"),
    CYCLING_MOUNTAIN("cycling-mountain"),
    CYCLING_ELECTRIC("cycling-electric"),
    FOOT_WALKING("foot-walking"),
    FOOT_HIKING("foot-hiking"),
    WHEELCHAIR("wheelchair");

    private final String mode;

    public static DirectionMode fromMode(String mode) {
        return Arrays.stream(DirectionMode.values())
                .filter(enumMode -> enumMode.getMode().equalsIgnoreCase(mode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid driving mode: " + mode));
    }

}