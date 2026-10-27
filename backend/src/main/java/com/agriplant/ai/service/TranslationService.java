
package com.agriplant.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${translation.libretranslate.url:http://localhost:5001/translate}")
    private String libreUrl;

    @Value("${translation.libretranslate.api-key:}")
    private String libreApiKey;

    private static final Set<String> SUPPORTED_LANGUAGES =
            Set.of("en", "hi", "mr", "gu");

    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank()) {
            return text;
        }

        if (targetLang == null ||
                !SUPPORTED_LANGUAGES.contains(targetLang)) {
            throw new IllegalArgumentException(
                "Supported languages: en, hi, mr, gu, ta, bn"
            );
        }

        if ("en".equals(targetLang)) {
            return text;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("q", text);
            body.put("source", "auto");
            body.put("target", targetLang);
            body.put("format", "text");

            if (libreApiKey != null && !libreApiKey.isBlank()) {
                body.put("api_key", libreApiKey);
            }

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response =
                    restTemplate.postForObject(
                            libreUrl, request, Map.class);

            if (response != null &&
                    response.get("translatedText") != null) {
                return response.get("translatedText").toString();
            }
        } catch (Exception ignored) {
            // Keep the application working if translation is unavailable.
        }

        return text;
    }
}
