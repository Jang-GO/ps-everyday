package org.janggo.pseveryday.external.solvedac.service;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.external.solvedac.client.SolvedAcClient;
import org.janggo.pseveryday.external.solvedac.dto.SolvedAcProperties;
import org.janggo.pseveryday.domain.problem.dto.SolvedAcResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SolvedAcService {
    private final SolvedAcProperties properties;
    private final SolvedAcClient solvedAcClient;
    private static final String RANDOM_QUERY = " ";

    public SolvedAcResponse.ProblemItem getRandomProblem(){
        Random random = new Random();
        int randomPage = random.nextInt(properties.getStart(), properties.getEnd()+1);
        SolvedAcResponse response = solvedAcClient.searchProblem(RANDOM_QUERY, "id", "asc", randomPage);
        List<SolvedAcResponse.ProblemItem> problems = response.getItems();

        if (problems == null || problems.isEmpty()) {
            throw new RuntimeException("검색된 문제가 없습니다.");
        }

        // 랜덤으로 문제 선택
        return problems.get(random.nextInt(problems.size()));
    }
}
