package org.janggo.pseveryday.problem.solvedac;

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

    // query=tier:11. => 11레벨 문제 가져오기
    // query=tier:11..15 => 11에서 15레벨 사이의 문제 가져오기
    // query=tier:11..15 tag:dp=> 11에서 15레벨 사이의 dp카테고리가 붙은 문제 가져오기
}
