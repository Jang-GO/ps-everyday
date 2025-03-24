package org.janggo.pseveryday.problem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class SolvedAcResponse {
    private List<Problem> items;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Problem {
        private int problemId;
        private String titleKo;
        private ProblemLevel level;
    }
}
