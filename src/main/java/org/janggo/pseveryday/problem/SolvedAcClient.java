package org.janggo.pseveryday.problem;

import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "solvedAc", url = "https://solved.ac/api/v3")
public interface SolvedAcClient {

    @GetMapping("/search/problem")
    SolvedAcResponse searchProblem(
            @RequestParam("query") String query,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction,
            @RequestParam("page") Integer page);
}
