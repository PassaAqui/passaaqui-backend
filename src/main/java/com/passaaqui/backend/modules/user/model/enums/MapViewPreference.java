package com.passaaqui.backend.modules.user.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MapViewPreference {
    MAP_2D("2D"),
    MAP_3D("3D");

    private final String value;

    MapViewPreference(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MapViewPreference fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (MapViewPreference pref : values()) {
            if (pref.value.equalsIgnoreCase(value) || pref.name().equalsIgnoreCase(value)) {
                return pref;
            }
        }
        throw new IllegalArgumentException("Unknown map view preference: " + value);
    }
}
