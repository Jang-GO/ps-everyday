package org.janggo.pseveryday.problem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class SolvedAcResponse {
    private List<Problem> items;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Problem {
        private int problemId;
        private String titleKo;
        private int level;
    }
}
