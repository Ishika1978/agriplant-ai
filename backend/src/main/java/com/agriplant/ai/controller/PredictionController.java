package com.agriplant.ai.controller;

import com.agriplant.ai.dto.PredictionResponse;
import com.agriplant.ai.repository.CareGuideRepository;
import com.agriplant.ai.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
public class PredictionController {

    private final PredictionService predictionService;
    private final CareGuideRepository careGuideRepository;

    public PredictionController(PredictionService predictionService,
                                 CareGuideRepository careGuideRepository) {
        this.predictionService = predictionService;
        this.careGuideRepository = careGuideRepository;
    }

    /**
     * POST /api/predictions?userId=1
     * multipart/form-data, field name "photo"
     *
     * The crop species is identified from the image by the CNN, not supplied
     * by the caller. A photo the model doesn't recognise as one of its trained
     * crops comes back 422 rather than being forced into a label.
     */
    @PostMapping
    public ResponseEntity<PredictionResponse> classify(
            @RequestParam Long userId,
            @RequestParam("photo") MultipartFile photo) throws IOException {

        PredictionService.Result result = predictionService.classifyAndSave(userId, photo);
        var guide = careGuideRepository
                .findByDisease_DiseaseId(result.prediction().getDisease().getDiseaseId())
                .orElse(null);

        return ResponseEntity.ok(
                PredictionResponse.from(result.prediction(), result.plantConfidence(), guide));
    }
}
