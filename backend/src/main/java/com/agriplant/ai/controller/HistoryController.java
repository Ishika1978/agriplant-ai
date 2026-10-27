package com.agriplant.ai.controller;

import com.agriplant.ai.model.HealthHistory;
import com.agriplant.ai.model.Prediction;
import com.agriplant.ai.repository.HealthHistoryRepository;
import com.agriplant.ai.repository.PredictionRepository;
import com.agriplant.ai.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HealthHistoryRepository historyRepository;
    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;

    public HistoryController(
            HealthHistoryRepository historyRepository,
            PredictionRepository predictionRepository,
            UserRepository userRepository) {

        this.historyRepository = historyRepository;
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
    }

    // GET /api/history?userId=1
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> forUser(
            @RequestParam Long userId) {

        List<HealthHistory> history =
                historyRepository.findByUser_UserIdOrderByRecordedAtDesc(userId);

        List<Map<String, Object>> result = history.stream().map(entry -> {

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("historyId", entry.getHistoryId());
            item.put("userId", entry.getUser().getUserId());
            item.put("predictionId", entry.getPrediction().getPredictionId());
            item.put("notes", entry.getNotes());
            item.put("recordedAt", entry.getRecordedAt());

            Prediction prediction = entry.getPrediction();

            if (prediction != null) {

                item.put(
                        "confidence",
                        prediction.getConfidence()
                );

                if (prediction.getSpecies() != null) {
                    item.put(
                            "plant",
                            prediction.getSpecies().getNameEn()
                    );

                    item.put(
                            "plantHi",
                            prediction.getSpecies().getNameHi()
                    );
                }

                if (prediction.getDisease() != null) {
                    item.put(
                            "disease",
                            prediction.getDisease().getNameEn()
                    );

                    item.put(
                            "diseaseHi",
                            prediction.getDisease().getNameHi()
                    );
                }
            }

            return item;

        }).toList();

        return ResponseEntity.ok(result);
    }

    // POST /api/history?userId=1&predictionId=5
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(
            @RequestParam Long userId,
            @RequestParam Long predictionId) {

        // Check that the user exists
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "unknown_user",
                            "message", "User not found: " + userId
                    ));
        }

        // Find the prediction
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown prediction: " + predictionId
                        )
                );

        // Make sure this prediction belongs to this user
        if (prediction.getUser() == null ||
                !userId.equals(prediction.getUser().getUserId())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "prediction_user_mismatch",
                            "message",
                            "This prediction does not belong to user " + userId
                    ));
        }

        // Create history record
        HealthHistory entry = new HealthHistory();

        entry.setUser(
                userRepository.getReferenceById(userId)
        );

        entry.setPrediction(prediction);

        entry.setRecordedAt(
                LocalDateTime.now()
        );

        HealthHistory saved = historyRepository.save(entry);

        // Return a simple JSON response instead of the JPA entity
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", true);
        response.put("message", "Prediction saved to health history");
        response.put("historyId", saved.getHistoryId());
        response.put("userId", userId);
        response.put("predictionId", predictionId);
        response.put("recordedAt", saved.getRecordedAt());

        return ResponseEntity.ok(response);
    }
        // DELETE /api/history?userId=1
    @DeleteMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> clearHistory(
            @RequestParam Long userId) {

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "unknown_user",
                            "message", "User not found: " + userId
                    ));
        }

        historyRepository.deleteByUser_UserId(userId);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Health history cleared successfully"
                )
        );
    }
}