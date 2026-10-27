package com.agriplant.ai.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private PlantSpecies species;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(name = "model_version")
    private String modelVersion = "cnn-v1";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getPredictionId() { return predictionId; }
    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public PlantSpecies getSpecies() { return species; }
    public void setSpecies(PlantSpecies species) { this.species = species; }
    public Disease getDisease() { return disease; }
    public void setDisease(Disease disease) { this.disease = disease; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
