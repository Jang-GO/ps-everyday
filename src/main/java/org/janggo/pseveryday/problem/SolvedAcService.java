package org.janggo.pseveryday.problem;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.problem.dto.SolvedAcProperties;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SolvedAcService {
    private final SolvedAcProperties properties;
    private final SolvedAcClient solvedAcClient;

    public SolvedAcResponse.Problem getRandomProblem(String query){
        Random random = new Random();
        int randomPage = random.nextInt(properties.getStart(), properties.getEnd()+1);
        SolvedAcResponse response = solvedAcClient.searchProblem(" ", "id", "asc", randomPage);
        List<SolvedAcResponse.Problem> problems = response.getItems();

        if (problems == null || problems.isEmpty()) {
            throw new RuntimeException("검색된 문제가 없습니다.");
        }

        // 랜덤으로 문제 선택
        return problems.get(random.nextInt(problems.size()));
    }
}
