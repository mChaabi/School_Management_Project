package com.example.School.Management.System.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
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
    private final List<String> models;

    public AiClient(@Value("${app.ai.api-key:}") String apiKey,
                    @Value("${app.ai.enabled:true}") boolean enabled,
                    @Value("${app.ai.model:gemini-3.8-flash}") String model,
                    @Value("${app.ai.fallback-models:}") String fallbacks,
                    @Value("${app.ai.max-tokens:200}") int maxTokens,
                    @Value("${app.ai.timeout-seconds:10}") int timeout) {

        if (apiKey == null || apiKey.isBlank()) {
            log.error("❌ [AI] API Key is EMPTY! Check if secrets.properties exists in src/main/resources/");
        } else {
            log.info("🔑 [AI] API Key loaded successfully (length: {})", apiKey.length());
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeout));
        factory.setReadTimeout(Duration.ofSeconds(timeout));

        this.http = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(factory)
                .defaultHeader("x-goog-api-key", apiKey != null ? apiKey.trim() : "")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.enabled = enabled && apiKey != null && !apiKey.isBlank();
        this.model = model;
        this.maxTokens = maxTokens;
        this.models = new java.util.ArrayList<>();
        this.models.add(model);
        for (String m : fallbacks.split(",")) if (!m.isBlank()) this.models.add(m.trim());

        if (this.enabled) log.info("🤖 [AI] Gemini enabled, model={}", model);
        else log.info("🤖 [AI] disabled (no key or turned off)");
    }

    public Optional<String> complete(String system, String user) {
        return complete(system, user, maxTokens, 0.4);
    }

    public Optional<String> complete(String system, String user, int maxOutputTokens, double temperature) {
        if (!enabled) return Optional.empty();

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                "contents", List.of(Map.of("role", "user",
                        "parts", List.of(Map.of("text", user)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", maxOutputTokens,
                        "temperature", temperature));

        for (String m : models) {
            long delay = 1000;
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    JsonNode res = http.post()
                            .uri("/v1beta/models/{model}:generateContent", m)
                            .body(body).retrieve().body(JsonNode.class);

                    String text = res == null ? "" : res.path("candidates").path(0)
                            .path("content").path("parts").path(0).path("text").asText("").trim();
                    if (!text.isBlank()) return Optional.of(text);

                    log.warn("⚠️ [AI] {} returned empty text: {}", m, res);
                    break; // next model

                } catch (org.springframework.web.client.HttpStatusCodeException ex) {
                    int code = ex.getStatusCode().value();
                    if (code == 404) { log.error("❌ [AI] Model '{}' not found", m); break; }
                    boolean retryable = code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
                    if (!retryable) {   // 400/401/403: key or request problem
                        log.error("❌ [AI] {} -> {} {}", m, code, ex.getResponseBodyAsString());
                        return Optional.empty();
                    }
                    log.warn("⚠️ [AI] {} -> {} (attempt {}/2)", m, code, attempt);
                    if (attempt < 2) sleep(delay);
                    delay *= 2;

                } catch (org.springframework.web.client.ResourceAccessException ex) { // timeout
                    log.warn("⚠️ [AI] {} timeout/network: {}", m, ex.getMessage());
                    break; // next model
                } catch (Exception ex) {
                    log.error("❌ [AI] {} failed: {}", m, ex.getMessage(), ex);
                    break;
                }
            }
            log.warn("↪️ [AI] switching to next model...");
        }
        log.error("❌ [AI] all models failed");
        return Optional.empty();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }}