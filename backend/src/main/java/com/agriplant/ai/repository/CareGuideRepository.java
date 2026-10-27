package com.agriplant.ai.repository;

import com.agriplant.ai.model.CareGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CareGuideRepository extends JpaRepository<CareGuide, Integer> {
    Optional<CareGuide> findByDisease_DiseaseId(Integer diseaseId);
}
