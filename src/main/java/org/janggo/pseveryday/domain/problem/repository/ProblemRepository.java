package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Problem 저장소
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query("SELECT DISTINCT p FROM Problem p " +
            "JOIN FETCH p.problemTags pt " +
            "JOIN FETCH pt.tag t " +
            "WHERE p.level BETWEEN :minLevel AND :maxLevel " +
            "AND t.id IN (SELECT tp.tag.id FROM TagPreference tp " +
            "            WHERE tp.subscriber.id = :subscriberId)")
    List<Problem> findProblemsBySubscriberPreferences(
            @Param("minLevel") int minLevel,
            @Param("maxLevel") int maxLevel,
            @Param("subscriberId") Long subscriberId);

    @Query("SELECT DISTINCT p FROM Problem p " +
            "JOIN FETCH p.problemTags pt " +
            "JOIN FETCH pt.tag t " +
            "WHERE t.id IN (SELECT tp.tag.id FROM TagPreference tp " +
            "              WHERE tp.subscriber.id = :subscriberId)")
    List<Problem> findByTagPreferences(@Param("subscriberId") Long subscriberId);

    @EntityGraph(attributePaths = {"problemTags", "problemTags.tag"})
    List<Problem> findByLevelBetween(int minLevel, int maxLevel);

    @EntityGraph(attributePaths = {"problemTags", "problemTags.tag"})
    List<Problem> findAll();
}
