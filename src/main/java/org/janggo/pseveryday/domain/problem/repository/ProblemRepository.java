package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Problem 저장소
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByLevelBetween(int minLevel, int maxLevel);
}
