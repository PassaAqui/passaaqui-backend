package br.com.recifego.api.modules.shopkeeper.dto;

public record UpdateShopkeeperDTO(
    String name, 
    String password, 
    String documentId, 
    String companyName
) {}