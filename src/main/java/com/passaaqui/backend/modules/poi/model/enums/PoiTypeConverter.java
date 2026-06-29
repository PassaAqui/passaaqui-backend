package com.passaaqui.backend.modules.poi.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PoiTypeConverter implements AttributeConverter<PoiType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PoiType attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case STORE -> 0;
            case TOURIST_POINT -> 1;
        };
    }

    @Override
    public PoiType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return switch (dbData) {
            case 0 -> PoiType.STORE;
            case 1 -> PoiType.TOURIST_POINT;
            default -> throw new IllegalArgumentException("Unknown PoiType value: " + dbData);
        };
    }
}
