package com.aiinterview.assistant.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class AiClientConfig {

    
    @Bean
    public HttpClient aiHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Build the ObjectMapper via Spring Boot's own Jackson2ObjectMapperBuilder
     * (rather than `new ObjectMapper()`) so it keeps Spring Boot's default
     * modules registered - notably JavaTimeModule, which is required to
     * serialize LocalDateTime fields (e.g. in error responses). A bare
     * `new ObjectMapper()` bean here would silently replace Spring's
     * auto-configured one app-wide and break that serialization.
     */
    @Bean
    public ObjectMapper objectMapper(ObjectProvider<Jackson2ObjectMapperBuilder> builderProvider) {
        Jackson2ObjectMapperBuilder builder = builderProvider.getIfAvailable(Jackson2ObjectMapperBuilder::new);
        return builder.build();
    }
}
