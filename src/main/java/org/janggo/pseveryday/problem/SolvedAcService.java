package org.janggo.pseveryday.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SolvedAcService {
    private final SolvedAcClient solvedAcClient;

    public SolvedAcResponse.Problem getRandomProblem(String query){
        SolvedAcResponse response = solvedAcClient.searchProblem(query, "id", "asc");
        List<SolvedAcResponse.Problem> problems = response.getItems();

        if (problems == null || problems.isEmpty()) {
            throw new RuntimeException("검색된 문제가 없습니다.");
        }

        // 랜덤으로 문제 선택
        Random random = new Random();
        return problems.get(random.nextInt(problems.size()));
    }
}
