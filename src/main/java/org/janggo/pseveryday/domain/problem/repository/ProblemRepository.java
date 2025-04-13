package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Problem 저장소
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByLevelBetween(int minLevel, int maxLevel);

    @Query("SELECT p FROM Problem p JOIN FETCH p.problemTags pt JOIN FETCH pt.tag WHERE p.level BETWEEN :minLevel AND :maxLevel")
    List<Problem> findByLevelBetweenWithTags(@Param("minLevel") int minLevel, @Param("maxLevel") int maxLevel);
}
