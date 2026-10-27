package com.agriplant.ai.controller;

import com.agriplant.ai.model.PlantSpecies;
import com.agriplant.ai.model.Scheme;
import com.agriplant.ai.model.User;
import com.agriplant.ai.service.SchemeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
public class SchemeController {

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    /**
     * GET /api/schemes/eligible?landholding=small&cropCategory=horticulture&hasCropLoss=true&hasSoilCard=false
     */
    @GetMapping("/eligible")
    public List<Scheme> eligible(@RequestParam User.Landholding landholding,
                                  @RequestParam PlantSpecies.Category cropCategory,
                                  @RequestParam(defaultValue = "false") boolean hasCropLoss,
                                  @RequestParam(defaultValue = "false") boolean hasSoilCard) {
        return schemeService.findEligible(landholding, cropCategory, hasCropLoss, hasSoilCard);
    }
}
