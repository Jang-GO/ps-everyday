package org.janggo.pseveryday.domain.subscriber.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.janggo.pseveryday.domain.problem.dto.ProblemLevel;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TierPreference {
    private Integer minTier;
    private Integer maxTier;

    // 최소 티어 이름 가져오기
    public String getMinTierName() {
        if (minTier == null) {
            return "선택 안함";
        }
        return ProblemLevel.getNameByLevel(minTier).orElse("선택 안함");
    }

    // 최대 티어 이름 가져오기
    public String getMaxTierName() {
        if (maxTier == null) {
            return "선택 안함";
        }
        return ProblemLevel.getNameByLevel(maxTier).orElse("선택 안함");
    }
}
