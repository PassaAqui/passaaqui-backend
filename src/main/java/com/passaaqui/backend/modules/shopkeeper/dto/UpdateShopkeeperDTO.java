package com.passaaqui.backend.modules.shopkeeper.dto;

import com.passaaqui.backend.modules.user.model.enums.ThemePreference;

public record UpdateShopkeeperDTO(
    String name,
    String password,
    String documentId,
    String companyName,
    String description,
    Integer categoryId,
    ThemePreference theme,
    String poiName,
    String poiDescription
) {
    public UpdateShopkeeperDTO(String name, String password, String documentId, String companyName, String description, Integer categoryId) {
        this(name, password, documentId, companyName, description, categoryId, null, null, null);
    }

    public UpdateShopkeeperDTO(String name, String password, String documentId, String companyName, String description, Integer categoryId, ThemePreference theme) {
        this(name, password, documentId, companyName, description, categoryId, theme, null, null);
    }
}