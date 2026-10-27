
package com.agriplant.ai.service;

import com.agriplant.ai.model.PlantSpecies;
import com.agriplant.ai.model.Scheme;
import com.agriplant.ai.model.User;
import com.agriplant.ai.repository.SchemeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;

    public SchemeService(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    public List<Scheme> findEligible(
            User.Landholding landholding,
            PlantSpecies.Category cropCategory,
            boolean hasCropLoss,
            boolean hasSoilCard) {

        return schemeRepository.findAll().stream()
                .filter(s -> {
                    String code = s.getCode();

                    // PM-KISAN: potentially relevant, but land ownership
                    // and other official eligibility conditions are not checked.
                    if ("PMKISAN".equals(code)) {
                        return true;
                    }

                    // Crop insurance: show only when crop loss is reported.
                    if ("PMFBY".equals(code)) {
                        return hasCropLoss;
                    }

                    // Soil Health Card: show only if the farmer does not
                    // already have a card.
                    if ("SOILHEALTH".equals(code)) {
                        return !hasSoilCard;
                    }

                    // Horticulture-related schemes.
                    if ("MIDH".equals(code)
                            || "NHB".equals(code)
                            || "HORTIMIDH".equals(code)) {
                        return cropCategory
                                == PlantSpecies.Category.horticulture;
                    }

                    // Food-grain scheme.
                    if ("NFSM".equals(code)) {
                        return cropCategory
                                == PlantSpecies.Category.foodgrain;
                    }

                    // Kisan Credit Card: candidate for cash-crop farmers.
                    if ("KCC".equals(code)
                            || "KCCEXT".equals(code)) {
                        return cropCategory
                                == PlantSpecies.Category.cash_crop;
                    }

                    // Oil-palm support: crop category alone cannot confirm
                    // that the farmer actually grows oil palm.
                    // Hide it until the form collects the specific crop.
                    if ("OILPALM".equals(code)) {
                        return false;
                    }

                    // These require information the current form does not
                    // collect, such as irrigation, machinery, organic farming,
                    // infrastructure, or marketing needs.
                    return false;
                })
                .filter(s -> s.getMinLandholding() == null
                        || s.getMinLandholding() == landholding)
                .filter(s -> s.getCropCategory() == null
                        || s.getCropCategory() == cropCategory)
                .toList();
    }
}
