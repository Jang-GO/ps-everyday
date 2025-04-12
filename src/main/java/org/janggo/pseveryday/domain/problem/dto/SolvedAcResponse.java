package org.janggo.pseveryday.domain.problem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolvedAcResponse {
    private List<ProblemItem> items;
    private Integer count;  // 총 문제 수

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
            private String key;
            private List<DisplayName> displayNames;

            @Getter
            @Setter
            public static class DisplayName {
                private String language;
                private String name;
                private String shortName;
            }
        }
    }
}