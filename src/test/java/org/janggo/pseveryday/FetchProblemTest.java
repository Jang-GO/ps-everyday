package org.janggo.pseveryday;

import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.domain.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.domain.problem.repository.TagRepository;
import org.janggo.pseveryday.external.solvedac.client.SolvedAcClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Slf4j
class FetchProblemTest {
    @Autowired
    private SolvedAcClient solvedAcClient;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
//    @Transactional
    void initProblems() {
        // 모든 태그에 대해 문제 업데이트
        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            updateProblemsByTag(tag.getKey());
        }
    }

    private void updateProblemsByTag(String tagKey) {
        String query = "tag:" + tagKey;

        // 첫 페이지 요청으로 총 문제 수 확인
        SolvedAcResponse firstPage = solvedAcClient.searchProblem(query, "id", "asc", 1);
        int totalCount = firstPage.getCount();
        int itemsPerPage = 50;  // 한 페이지당 50개
        int totalPages = (int) Math.ceil((double) totalCount / itemsPerPage);

        // 모든 페이지 순회
        for (int page = 1; page <= totalPages; page++) {
            SolvedAcResponse response = solvedAcClient.searchProblem(query, "id", "asc", page);

            for (SolvedAcResponse.ProblemItem item : response.getItems()) {
                // 이미 존재하는 문제는 건너뛰기
                if (problemRepository.existsById(item.getProblemId())) {
                    log.info("이미 존재하는 문제 ([{}], {})", item.getProblemId(), item.getTitleKo());
                    continue;
                }

                Problem problem = new Problem(item.getProblemId(), item.getTitleKo(), item.getLevel());

                for (SolvedAcResponse.ProblemItem.TagItem tagItem : item.getTags()) {
                    Optional<Tag> existingTag = tagRepository.findByKey(tagItem.getKey());
                    existingTag.ifPresent(problem::addTag);
                }

                problemRepository.save(problem);
            }

            // API 호출 간격을 두기 위해 잠시 대기
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}