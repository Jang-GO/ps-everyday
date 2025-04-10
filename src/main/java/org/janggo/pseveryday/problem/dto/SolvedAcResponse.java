package org.janggo.pseveryday.problem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolvedAcResponse {
    private List<ProblemItem> items;

    @Getter
    @Setter
    public static class ProblemItem {
        private Long problemId;
        private String titleKo;
        private Integer level;
        private List<TagItem> tags;

        @Getter
        @Setter
        public static class TagItem {
            private String displayNames;
        }
    }
}