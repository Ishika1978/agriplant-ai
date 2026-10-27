package com.agriplant.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plant_species")
public class PlantSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "species_id")
    private Integer speciesId;

    @Column(name = "name_en", nullable = false, length = 80)
    private String nameEn;

    @Column(name = "name_hi", nullable = false, length = 80)
    private String nameHi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    public enum Category { foodgrain, horticulture, cash_crop }

    public Integer getSpeciesId() { return speciesId; }
    public void setSpeciesId(Integer speciesId) { this.speciesId = speciesId; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameHi() { return nameHi; }
    public void setNameHi(String nameHi) { this.nameHi = nameHi; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
