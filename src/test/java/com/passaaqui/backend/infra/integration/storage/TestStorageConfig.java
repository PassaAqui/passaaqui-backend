package com.passaaqui.backend.infra.integration.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestStorageConfig {

    @Bean
    StorageService storageService() {
        return mock(StorageService.class);
    }

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
