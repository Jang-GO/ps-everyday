package org.janggo.pseveryday.problem;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems")
public class SolvedAcController {
    private final SolvedAcService solvedAcService;
    private static final String RANDOM_QUERY = " ";
    @GetMapping("/random")
    public ResponseEntity<SolvedAcResponse.Problem> getRandomProblem() {
        return ResponseEntity.ok(solvedAcService.getRandomProblem(RANDOM_QUERY));
    }
}
