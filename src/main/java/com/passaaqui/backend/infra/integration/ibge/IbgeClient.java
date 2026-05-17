package com.passaaqui.backend.infra.integration.ibge;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class IbgeClient {
    private final WebClient webClient;

    public IbgeClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://servicodados.ibge.gov.br/api/v1/localidades/municipios").build();
    }

    public IbgeCityResponse getCityByIbgeCode(String ibgeCode) {
        return webClient.get()
                .retrieve()
                .bodyToFlux(IbgeCityResponse.class)
                .filter(city -> city.getId().equals(Long.valueOf(ibgeCode)))
                .next()
                .block();
    }

}
