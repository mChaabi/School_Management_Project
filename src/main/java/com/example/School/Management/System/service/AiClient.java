package com.example.School.Management.System.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AiClient {

    private final RestClient http;
    private final boolean enabled;
    private final String model;
    private final int maxTokens;

    public AiClient(@Value("${app.ai.api-key:}") String apiKey,
                    @Value("${app.ai.enabled:true}") boolean enabled,
                    @Value("${app.ai.model}") String model,
                    @Value("${app.ai.max-tokens:200}") int maxTokens,
                    @Value("${app.ai.timeout-seconds:10}") int timeout) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeout));
        factory.setReadTimeout(Duration.ofSeconds(timeout));

        this.http = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(factory)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.enabled = enabled && !apiKey.isBlank();
        this.model = model;
        this.maxTokens = maxTokens;

        if (this.enabled) log.info("🤖 [AI] Gemini enabled, model={}", model);
        else log.info("🤖 [AI] disabled (no key or turned off), fallback texts will be used");
    }

    /** Returns the text, or empty if disabled or failed. Never throws. */
    public Optional<String> complete(String system, String user) {
        if (!enabled) return Optional.empty();
        try {
            Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", user)))),
                    "generationConfig", Map.of(
                            "maxOutputTokens", maxTokens,
                            "temperature", 0.4));

            JsonNode res = http.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            String text = res == null ? "" : res.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text").asText("").trim();

            return text.isBlank() ? Optional.empty() : Optional.of(text);

        } catch (Exception e) {
            log.warn("⚠️ [AI] Gemini call failed, using fallback: {}", e.getMessage());
            return Optional.empty();
        }
    }
}