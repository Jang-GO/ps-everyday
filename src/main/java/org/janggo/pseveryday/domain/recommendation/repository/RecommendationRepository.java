package org.janggo.pseveryday.domain.recommendation.repository;

import org.janggo.pseveryday.domain.recommendation.dto.RecommendationProjection;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT r.problem.problemId FROM Recommendation r WHERE r.subscriber = :subscriber")
    Set<Long> findRecommendedProblemIdsBySubscriber(@Param("subscriber") Subscriber subscriber);

    @Query("SELECT r FROM Recommendation r JOIN FETCH r.problem WHERE r.subscriber = :subscriber ORDER BY r.recommendedAt DESC")
    List<Recommendation> findBySubscriberWithProblem(@Param("subscriber") Subscriber subscriber);

    Page<Recommendation> findBySubscriberOrderByRecommendedAtDesc(Subscriber subscriber, Pageable pageable);

     @Query("SELECT r.subscriber.id as subscriberId, r.problem.problemId as problemId FROM Recommendation r")
     List<RecommendationProjection> findAllSubscriberProblemPairs();
}
