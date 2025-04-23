package org.janggo.pseveryday.domain.recommendation.repository;

import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
