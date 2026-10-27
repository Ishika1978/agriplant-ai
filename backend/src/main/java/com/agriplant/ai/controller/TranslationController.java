package com.agriplant.ai.controller;

import com.agriplant.ai.service.TranslationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * POST /api/translate  body: {"text": "...", "target": "hi"}
     * Used for dynamic content only (e.g. a scheme notice pulled from an
     * external feed, or a farmer's own free-text note) — static UI
     * strings are pre-translated in the frontend dictionary.
     */
    @PostMapping
    public Map<String, String> translate(@RequestBody Map<String, String> body) {
        String translated = translationService.translate(body.get("text"), body.get("target"));
        return Map.of("translatedText", translated);
    }
}
