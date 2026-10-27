package com.agriplant.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "care_guides")
public class CareGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "care_id")
    private Integer careId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(name = "water_en") private String waterEn;
    @Column(name = "water_hi") private String waterHi;
    @Column(name = "sunlight_en") private String sunlightEn;
    @Column(name = "sunlight_hi") private String sunlightHi;
    @Column(name = "fertilizer_en") private String fertilizerEn;
    @Column(name = "fertilizer_hi") private String fertilizerHi;
    @Column(name = "next_step_en") private String nextStepEn;
    @Column(name = "next_step_hi") private String nextStepHi;

    public Integer getCareId() { return careId; }
    public Disease getDisease() { return disease; }
    public String getWaterEn() { return waterEn; }
    public String getWaterHi() { return waterHi; }
    public String getSunlightEn() { return sunlightEn; }
    public String getSunlightHi() { return sunlightHi; }
    public String getFertilizerEn() { return fertilizerEn; }
    public String getFertilizerHi() { return fertilizerHi; }
    public String getNextStepEn() { return nextStepEn; }
    public String getNextStepHi() { return nextStepHi; }
}
