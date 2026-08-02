package com.passaaqui.backend.modules.admin.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AdminTypeConverter implements AttributeConverter<AdminType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AdminType attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case USER -> 0;
            case ROOT -> 1;
        };
    }

    @Override
    public AdminType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return switch (dbData) {
            case 0 -> AdminType.USER;
            case 1 -> AdminType.ROOT;
            default -> throw new IllegalArgumentException("Unknown AdminType value: " + dbData);
        };
    }
}
