package com.passaaqui.backend.modules.tourist.dto;

public record UpdateTouristDTO(
    String name, 
    String password, 
    String documentId
) {}