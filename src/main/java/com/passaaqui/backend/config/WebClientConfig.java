package com.passaaqui.backend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 3000;
    private static final int READ_TIMEOUT_SECONDS = 5;

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}