package org.janggo.pseveryday.problem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems")
public class SolvedAcController {
    private final SolvedAcService solvedAcService;

    @GetMapping("/random")
    public ResponseEntity<SolvedAcResponse.Problem> getRandomProblem(@RequestParam String query) {
        return ResponseEntity.ok(solvedAcService.getRandomProblem(query));
    }
}
