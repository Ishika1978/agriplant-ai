package com.agriplant.ai.repository;

import com.agriplant.ai.model.PlantSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlantSpeciesRepository extends JpaRepository<PlantSpecies, Integer> {
    Optional<PlantSpecies> findByNameEnIgnoreCase(String nameEn);
}
