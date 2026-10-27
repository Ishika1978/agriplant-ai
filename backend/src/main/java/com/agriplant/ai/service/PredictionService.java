package com.agriplant.ai.service;

import com.agriplant.ai.model.*;
import com.agriplant.ai.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates one leaf-photo prediction end to end:
 *   1. Save the uploaded photo to disk
 *   2. Forward it to the Python AI service (/ai-service) for CNN inference
 *   3. Resolve the returned class label against the diseases table
 *   4. Persist a Prediction row so it shows up in the farmer's history
 */
@Service
public class PredictionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PredictionRepository predictionRepository;
    private final DiseaseRepository diseaseRepository;
    private final UserRepository userRepository;

    @Value("${ai.service.base-url}")
    private String aiServiceBaseUrl;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public PredictionService(PredictionRepository predictionRepository,
                              DiseaseRepository diseaseRepository,
                              UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.diseaseRepository = diseaseRepository;
        this.userRepository = userRepository;
    }

    /** Prediction plus the species confidence the model reported. */
    public record Result(Prediction prediction, double plantConfidence) {}

    public Result classifyAndSave(Long userId, MultipartFile photo) throws IOException {
        String storedPath = storePhoto(photo);

        // Call the Python CNN service — see /ai-service/app.py
        Map<String, Object> inference = callAiService(photo);
        String classLabel = (String) inference.get("class_label");
        double confidenceValue = ((Number) inference.get("confidence")).doubleValue();

        // The model is a fixed 38-class softmax over 14 crops. If the summed
        // per-species probability is low, the photo is probably not one of
        // those crops at all (a houseplant, a weed, a blurry close-up), and
        // naming a crop disease for it would be actively misleading.
        if (Boolean.FALSE.equals(inference.get("plant_recognised"))) {
            throw new UnclearPredictionException(
                    "This doesn't look like one of the crops the model knows ("
                            + String.format("%.1f", ((Number) inference.get("plant_confidence")).doubleValue())
                            + "% species confidence). Supported crops: apple, blueberry, cherry, "
                            + "maize, grape, orange, peach, bell pepper, potato, raspberry, "
                            + "soybean, squash, strawberry, tomato.");
        }

        // Species is identified from the image, not chosen by the user.
        String identifiedPlant = (String) inference.get("plant");

        if (Boolean.FALSE.equals(inference.get("reliable"))) {
            throw new UnclearPredictionException(
                    "Identified the plant as " + identifiedPlant
                            + ", but could not determine the disease confidently ("
                            + String.format("%.1f", confidenceValue)
                            + "%). Try a closer, well-lit photo of a single affected leaf.");
        }

        Disease disease = diseaseRepository.findByClassLabel(classLabel)
                .orElseThrow(() -> new IllegalStateException(
                        "Unknown class label from AI service: " + classLabel
                        + ". Run ai-service/export_labels.py and load "
                        + "database/seed_classes.sql so every trained class has a row."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + userId));

        Prediction prediction = new Prediction();
        prediction.setUser(user);
        prediction.setSpecies(disease.getSpecies());
        prediction.setDisease(disease);
        prediction.setImagePath(storedPath);
        prediction.setConfidence(BigDecimal.valueOf(confidenceValue));
        prediction.setCreatedAt(LocalDateTime.now());

        double plantConfidence = inference.get("plant_confidence") == null ? 0
                : ((Number) inference.get("plant_confidence")).doubleValue();
        return new Result(predictionRepository.save(prediction), plantConfidence);
    }

    private String storePhoto(MultipartFile photo) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "-" + photo.getOriginalFilename();
        Path target = dir.resolve(filename);
        Files.copy(photo.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callAiService(MultipartFile photo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(photo.getBytes()) {
            @Override
            public String getFilename() {
                return photo.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(aiServiceBaseUrl + "/predict", request, Map.class);
    }
}
