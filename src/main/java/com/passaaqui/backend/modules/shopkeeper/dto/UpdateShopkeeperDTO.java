package com.passaaqui.backend.modules.shopkeeper.dto;

public record UpdateShopkeeperDTO(
    String name,
    String password,
    String documentId,
    String companyName,
    String description,
    Integer categoryId
) {}