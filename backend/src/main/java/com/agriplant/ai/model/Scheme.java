
package com.agriplant.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "schemes")
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheme_id")
    private Integer schemeId;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;

    @Column(name = "name_hi", nullable = false, length = 150)
    private String nameHi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_hi", columnDefinition = "TEXT")
    private String descriptionHi;

    @Enumerated(EnumType.STRING)
    @Column(name = "min_landholding")
    private User.Landholding minLandholding;

    @Enumerated(EnumType.STRING)
    @Column(name = "crop_category")
    private PlantSpecies.Category cropCategory;

    @Column(name = "requires_loss")
    private Boolean requiresLoss = false;

    @Column(name = "requires_no_soil_card")
    private Boolean requiresNoSoilCard = false;

    public Integer getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Integer schemeId) {
        this.schemeId = schemeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameHi() {
        return nameHi;
    }

    public void setNameHi(String nameHi) {
        this.nameHi = nameHi;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionHi() {
        return descriptionHi;
    }

    public void setDescriptionHi(String descriptionHi) {
        this.descriptionHi = descriptionHi;
    }

    public User.Landholding getMinLandholding() {
        return minLandholding;
    }

    public void setMinLandholding(User.Landholding minLandholding) {
        this.minLandholding = minLandholding;
    }

    public PlantSpecies.Category getCropCategory() {
        return cropCategory;
    }

    public void setCropCategory(PlantSpecies.Category cropCategory) {
        this.cropCategory = cropCategory;
    }

    public Boolean getRequiresLoss() {
        return requiresLoss;
    }

    public void setRequiresLoss(Boolean requiresLoss) {
        this.requiresLoss = requiresLoss;
    }

    public Boolean getRequiresNoSoilCard() {
        return requiresNoSoilCard;
    }

    public void setRequiresNoSoilCard(Boolean requiresNoSoilCard) {
        this.requiresNoSoilCard = requiresNoSoilCard;
    }
}
