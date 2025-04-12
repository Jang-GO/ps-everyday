package org.janggo.pseveryday.presentation;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.domain.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.external.solvedac.service.SolvedAcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems")
public class SolvedAcController {
    private final SolvedAcService solvedAcService;
    @GetMapping("/random")
    public ResponseEntity<SolvedAcResponse.ProblemItem> getRandomProblem() {
        return ResponseEntity.ok(solvedAcService.getRandomProblem());
    }
}
