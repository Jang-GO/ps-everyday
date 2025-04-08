package org.janggo.pseveryday.subscriber.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TierPreference {
    private Integer minTier;
    private Integer maxTier;

    public boolean hasPreference() {
        return minTier != null || maxTier != null;
    }

    public int getEffectiveMinTier() {
        return minTier != null ? minTier : 1; // 기본값은 1 (브론즈5)
    }

    public int getEffectiveMaxTier() {
        return maxTier != null ? maxTier : 30; // 기본값은 30 (루비1)
    }

}
