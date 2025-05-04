package org.janggo.pseveryday.domain.recommendation.dto;

import lombok.Getter;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;

import java.time.LocalDateTime;

@Getter
public class RecommendationDto {

    private final LocalDateTime recommendedAt;
    private final Long problemId;
    private final String titleKo;
    private final Integer level; // 정렬을 위한 숫자 레벨
    private final String levelDisplayName; // 표시를 위한 문자열 레벨 (예: "Silver V")

    public RecommendationDto(Recommendation recommendation) {
        this.recommendedAt = recommendation.getRecommendedAt();

        // Problem 객체가 null일 수 있으므로 안전하게 접근
        if (recommendation.getProblem() != null) {
            this.problemId = recommendation.getProblem().getProblemId();
            this.titleKo = recommendation.getProblem().getTitleKo();
            this.level = recommendation.getProblem().getLevel(); // Problem 엔티티에 level 필드가 있다고 가정

            // ProblemLevel 객체가 null일 수 있으므로 안전하게 접근
            if (recommendation.getProblem().getProblemLevel() != null) {
                this.levelDisplayName = recommendation.getProblem().getProblemLevel().getDisplayName();
            } else {
                this.levelDisplayName = "N/A"; // ProblemLevel이 없는 경우
            }
        } else {
            // Problem 정보가 없는 경우 기본값 설정
            this.problemId = null;
            this.titleKo = "N/A";
            this.level = null;
            this.levelDisplayName = "N/A";
        }
    }
}