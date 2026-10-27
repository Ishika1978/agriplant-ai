package com.agriplant.ai.repository;

import com.agriplant.ai.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
