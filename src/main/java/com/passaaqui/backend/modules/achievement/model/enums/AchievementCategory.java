package com.passaaqui.backend.modules.achievement.model.enums;

import lombok.Getter;

@Getter
public enum AchievementCategory {
    TUDO("Tudo", "Conquistas gerais, sem categoria específica"),
    SABORES_DA_MATA("Sabores da Mata", "Visitar restaurantes, lanchonetes e bares; provar pratos típicos"),
    GRAO_DE_OURO("Grão de Ouro", "Visitar padarias e cafeterias"),
    COLHEITA("Colheita", "Conhecer mercados e feiras locais"),
    SEIVA_VITAL("Seiva Vital", "Visitar farmácias e lojas de saúde parceiras"),
    INSTINTO_SELVAGEM("Instinto Selvagem", "Check-in em academias e centros esportivos"),
    FLORACAO("Floração", "Visitar salões, barbearias e clínicas de estética"),
    COMPANHEIROS_DA_MATA("Companheiros da Mata", "Visitar pet shops e cuidados animais"),
    RAIZES_DO_BRASIL("Raízes do Brasil", "Museus, teatros, igrejas históricas, patrimônio e eventos culturais"),
    DESBRAVADOR("Desbravador", "Praias, parques, mirantes e roteiros");

    private final String label;
    private final String description;

    AchievementCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
