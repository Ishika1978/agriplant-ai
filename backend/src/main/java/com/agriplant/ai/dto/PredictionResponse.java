package com.agriplant.ai.dto;

import com.agriplant.ai.model.Disease;
import com.agriplant.ai.model.PlantSpecies;
import com.agriplant.ai.model.Prediction;

/**
 * Flat, bilingual view of a prediction for the frontend.
 *
 * Returning the Prediction entity directly would lazy-load Disease and
 * PlantSpecies outside the transaction and leak the whole object graph
 * (including the User) into the JSON. This exposes exactly what the UI
 * renders: the identified crop, the disease, and the care guidance.
 */
public record PredictionResponse(
        Long predictionId,
        Species species,
        DiseaseInfo disease,
        Care care,
        double confidence,
        double plantConfidence,
        String modelVersion
) {
    public record Species(Integer speciesId, String nameEn, String nameHi, String category) {}

    public record DiseaseInfo(Integer diseaseId, String classLabel, String nameEn, String nameHi,
                               String descriptionEn, String descriptionHi, String severity) {}

    public record Care(String waterEn, String waterHi, String sunlightEn, String sunlightHi,
                        String fertilizerEn, String fertilizerHi,
                        String nextStepEn, String nextStepHi) {}

    public static PredictionResponse from(Prediction p, double plantConfidence,
                                           com.agriplant.ai.model.CareGuide guide) {
        PlantSpecies s = p.getSpecies();
        Disease d = p.getDisease();
        return new PredictionResponse(
                p.getPredictionId(),
                new Species(s.getSpeciesId(), s.getNameEn(), s.getNameHi(),
                        s.getCategory() == null ? null : s.getCategory().name()),
                new DiseaseInfo(d.getDiseaseId(), d.getClassLabel(), d.getNameEn(), d.getNameHi(),
                        d.getDescriptionEn(), d.getDescriptionHi(),
                        d.getSeverity() == null ? null : d.getSeverity().name()),
                guide == null ? null : new Care(
                        guide.getWaterEn(), guide.getWaterHi(),
                        guide.getSunlightEn(), guide.getSunlightHi(),
                        guide.getFertilizerEn(), guide.getFertilizerHi(),
                        guide.getNextStepEn(), guide.getNextStepHi()),
                p.getConfidence() == null ? 0 : p.getConfidence().doubleValue(),
                plantConfidence,
                p.getModelVersion());
    }
}
