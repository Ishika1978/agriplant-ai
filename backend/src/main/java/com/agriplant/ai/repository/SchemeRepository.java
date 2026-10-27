package com.agriplant.ai.repository;

import com.agriplant.ai.model.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemeRepository extends JpaRepository<Scheme, Integer> {
}
