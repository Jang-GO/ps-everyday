package org.janggo.pseveryday.domain.recommendation.repository;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcRecommendationRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
            "INSERT INTO recommendation (subscriber_id, problem_id, recommended_at) VALUES (?, ?, ?)";

    public void bulkInsert(List<Recommendation> recommendations) {
        jdbcTemplate.batchUpdate(INSERT_SQL,
                recommendations,
                100, // batch size (조절 가능)
                (ps, recommendation) -> {
                    ps.setLong(1, recommendation.getSubscriber().getId());
                    ps.setLong(2, recommendation.getProblem().getProblemId());
                    ps.setTimestamp(3, Timestamp.valueOf(recommendation.getRecommendedAt()));
                }
        );
    }
}
