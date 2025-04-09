package org.janggo.pseveryday.problem.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Problem 저장소
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Integer> {
    List<Problem> findByLevelBetween(int minLevel, int maxLevel);
}
