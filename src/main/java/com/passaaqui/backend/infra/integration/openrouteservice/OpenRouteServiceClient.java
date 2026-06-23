package com.passaaqui.backend.infra.integration.openrouteservice;

import com.passaaqui.backend.modules.direction.dto.DirectionRequestDTO;
import com.passaaqui.backend.modules.direction.model.enums.DirectionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OpenRouteServiceClient {

    private final WebClient webClient;

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    public OpenRouteServiceClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.openrouteservice.org/v2").build();
    }

    public Object getDirections(DirectionRequestDTO dto, DirectionMode mode) {
        String startParam = dto.startLongitude() + "," + dto.startLatitude();
        String endParam = dto.endLongitude() + "," + dto.endLatitude();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/directions/" + mode.getMode())
                        .queryParam("api_key", apiKey)
                        .queryParam("start", startParam)
                        .queryParam("end", endParam)
                        .queryParam("language", "pt-br")
                        .build())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}