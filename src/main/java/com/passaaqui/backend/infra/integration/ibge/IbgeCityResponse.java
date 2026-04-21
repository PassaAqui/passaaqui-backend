package com.passaaqui.backend.infra.integration.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IbgeCityResponse {

    private Long id;
    private String nome;
    private Microrregiao microrregiao;

    @JsonProperty("regiao-imediata")
    private RegiaoImediata regiaoImediata;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Microrregiao {
        private Long id;
        private String nome;
        private Mesorregiao mesorregiao;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mesorregiao {
        private Long id;
        private String nome;
        private UF UF;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UF {
        private Long id;
        private String sigla;
        private String nome;
        private Regiao regiao;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Regiao {
        private Long id;
        private String sigla;
        private String nome;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegiaoImediata {
        private Long id;
        private String nome;

        @JsonProperty("regiao-intermediaria")
        private RegiaoIntermediaria regiaoIntermediaria;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegiaoIntermediaria {
        private Long id;
        private String nome;
        private UF UF;
    }
}