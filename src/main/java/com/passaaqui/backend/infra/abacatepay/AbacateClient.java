package com.passaaqui.backend.infra.abacatepay;

import com.passaaqui.backend.infra.abacatepay.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AbacateClient {

    private final WebClient webClient;

    @Value("${abacatepay.api.key}")
    private String apiKey;

    public AbacateClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.abacatepay.com/v2").build();
    }

    public CheckoutResponseDTO createCheckout(CheckoutRequestDTO dto) {

        int priceInCents = (int) (dto.price() * 100);

        var apiPayload = Map.of(
                "method", "PIX",
                "data", Map.of(
                        "amount", priceInCents,
                        "expiresIn", 600,
                        "description", "Pagamento no PassaAqui",
                        "externalId", dto.id().toString()
                )
        );

        return webClient.post()
                .uri("/transparents/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(apiPayload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AbacateBaseResponseDTO<CheckoutResponseDTO>>() {
                })
                .map(AbacateBaseResponseDTO::data)
                .block();
    }

    public String checkPaymentStatus(String gatewayId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transparents/check")
                        .queryParam("id", gatewayId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AbacateBaseResponseDTO<CheckoutResponseDTO>>() {})
                .map(response -> response.data().status())
                .block();
    }
}