package com.agriplant.ai.repository;

import com.agriplant.ai.model.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
    Optional<Disease> findByClassLabel(String classLabel);
}
