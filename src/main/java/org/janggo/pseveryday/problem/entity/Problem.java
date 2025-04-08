package org.janggo.pseveryday.problem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;

import java.time.LocalDateTime;

// Problem 엔티티
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Problem {
    @Id
    private int problemId;

    private String titleKo;

    private int level;

    private LocalDateTime createdAt;

    // 필요하다면 태그 정보 등 추가 필드

    public static Problem fromSolvedAcProblem(SolvedAcResponse.Problem problem) {
        Problem entity = new Problem();
        entity.setProblemId(problem.getProblemId());
        entity.setTitleKo(problem.getTitleKo());
        entity.setLevel(problem.getLevel().getLevel());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}