package com.agriplant.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "diseases")
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disease_id")
    private Integer diseaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private PlantSpecies species;

    @Column(name = "class_label", nullable = false, unique = true, length = 120)
    private String classLabel;

    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;

    @Column(name = "name_hi", nullable = false, length = 120)
    private String nameHi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_hi", columnDefinition = "TEXT")
    private String descriptionHi;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.moderate;

    public enum Severity { none, moderate, severe }

    public Integer getDiseaseId() { return diseaseId; }
    public void setDiseaseId(Integer diseaseId) { this.diseaseId = diseaseId; }
    public PlantSpecies getSpecies() { return species; }
    public void setSpecies(PlantSpecies species) { this.species = species; }
    public String getClassLabel() { return classLabel; }
    public void setClassLabel(String classLabel) { this.classLabel = classLabel; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameHi() { return nameHi; }
    public void setNameHi(String nameHi) { this.nameHi = nameHi; }
    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionHi() { return descriptionHi; }
    public void setDescriptionHi(String descriptionHi) { this.descriptionHi = descriptionHi; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
}
