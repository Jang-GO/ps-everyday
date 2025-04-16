package org.janggo.pseveryday.domain.problem.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ProblemLevel {
    UNRATED(0, "Unrated", "#888888"),
    BRONZE_V(1, "Bronze V", "#CD7F32"),
    BRONZE_IV(2, "Bronze IV", "#CD7F32"),
    BRONZE_III(3, "Bronze III", "#CD7F32"),
    BRONZE_II(4, "Bronze II", "#CD7F32"),
    BRONZE_I(5, "Bronze I", "#CD7F32"),
    SILVER_V(6, "Silver V", "#C0C0C0"),
    SILVER_IV(7, "Silver IV", "#C0C0C0"),
    SILVER_III(8, "Silver III", "#C0C0C0"),
    SILVER_II(9, "Silver II", "#C0C0C0"),
    SILVER_I(10, "Silver I", "#C0C0C0"),
    GOLD_V(11, "Gold V", "#FFD700"),
    GOLD_IV(12, "Gold IV", "#FFD700"),
    GOLD_III(13, "Gold III", "#FFD700"),
    GOLD_II(14, "Gold II", "#FFD700"),
    GOLD_I(15, "Gold I", "#FFD700"),
    PLATINUM_V(16, "Platinum V", "#00E5EE"),
    PLATINUM_IV(17, "Platinum IV", "#00E5EE"),
    PLATINUM_III(18, "Platinum III", "#00E5EE"),
    PLATINUM_II(19, "Platinum II", "#00E5EE"),
    PLATINUM_I(20, "Platinum I", "#00E5EE"),
    DIAMOND_V(21, "Diamond V", "#1E90FF"),
    DIAMOND_IV(22, "Diamond IV", "#1E90FF"),
    DIAMOND_III(23, "Diamond III", "#1E90FF"),
    DIAMOND_II(24, "Diamond II", "#1E90FF"),
    DIAMOND_I(25, "Diamond I", "#1E90FF"),
    RUBY_V(26, "Ruby V", "#E0115F"),
    RUBY_IV(27, "Ruby IV", "#E0115F"),
    RUBY_III(28, "Ruby III", "#E0115F"),
    RUBY_II(29, "Ruby II", "#E0115F"),
    RUBY_I(30, "Ruby I", "#E0115F");

    private final int level;
    private final String displayName;
    private final String color;

    ProblemLevel(int level, String displayName, String color) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
    }

    @JsonCreator
    public static ProblemLevel fromLevel(int level) {
        for (ProblemLevel problemLevel : values()) {
            if (problemLevel.level == level) {
                return problemLevel;
            }
        }
        throw new IllegalArgumentException("Invalid level code: " + level);
    }
}
