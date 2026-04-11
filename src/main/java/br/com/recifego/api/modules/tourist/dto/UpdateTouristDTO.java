package br.com.recifego.api.modules.tourist.dto;

public record UpdateTouristDTO(
    String name, 
    String password, 
    String documentId
) {}