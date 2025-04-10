package org.janggo.pseveryday.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Problem 엔티티
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {
    @Id
    @Column(name = "problem_id")
    private Long problemId;

    private String titleKo;

    private Integer level;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemTag> problemTags = new ArrayList<>();

    public Problem(Long problemId, String titleKo, Integer level) {
        this.problemId = problemId;
        this.titleKo = titleKo;
        this.level = level;
        this.createdAt = LocalDateTime.now();
    }

    public void addTag(Tag tag) {
        ProblemTag problemTag = new ProblemTag(this, tag);
        this.problemTags.add(problemTag);
    }
}