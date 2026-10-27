package com.agriplant.ai.repository;

import com.agriplant.ai.model.HealthHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthHistoryRepository extends JpaRepository<HealthHistory, Long> {

    List<HealthHistory> findByUser_UserIdOrderByRecordedAtDesc(Long userId);

    // Delete all health-history records belonging to a user
    void deleteByUser_UserId(Long userId);
}