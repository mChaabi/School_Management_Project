package com.example.School.Management.System.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PromptTemplates {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String name) {
        return cache.computeIfAbsent(name, n -> {
            try (var in = new ClassPathResource("prompts/" + n + ".txt").getInputStream()) {
                return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Missing prompt file: prompts/" + n + ".txt", e);
            }
        });
    }

    public String render(String template, Map<String, String> facts) {
        return PLACEHOLDER.matcher(template)
                .replaceAll(m -> Matcher.quoteReplacement(clean(facts.get(m.group(1)))));
    }

    private String clean(String v) {
        if (v == null || v.isBlank()) return "not provided";
        String s = v.replaceAll("\\s+", " ").trim();
        return s.length() > 200 ? s.substring(0, 200) : s;
    }
}
