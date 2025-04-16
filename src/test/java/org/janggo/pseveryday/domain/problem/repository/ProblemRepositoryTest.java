package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.entity.TierPreference;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Transactional
class ProblemRepositoryTest {
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private TagRepository tagRepository;

    @Mock
    private MailService mailService;

    private Tag tag1;
    private Tag tag2;
    private Tag tag3;
    private Subscriber subscriber;
    private Problem problem1;
    private Problem problem2;
    private Problem problem3;
    private Problem problem4;

    @BeforeEach
    void setUp() {
        // 1. 태그 생성
        tag1 = new Tag("구현", "implementation");
        tag2 = new Tag("그리디", "greedy");
        tag3 = new Tag("수학", "math");
        tagRepository.save(tag1);
        tagRepository.save(tag2);
        tagRepository.save(tag3);

        // 2. 구독자 생성 및 선호 설정
        subscriber = new Subscriber("test@example.com", new TierPreference(1, 5)); // Bronze V ~ Bronze I

        subscriber.addTagPreference(tag1);
        subscriber.addTagPreference(tag2);

        subscriberRepository.save(subscriber);

        // 3. 문제 생성
        problem1 = new Problem(1L, "문제1",  1); // Bronze V
        problem1.addTag(tag1);
        problemRepository.save(problem1);

        problem2 = new Problem(2L, "문제2",  3); // Bronze III
        problem2.addTag(tag2);
        problemRepository.save(problem2);

        problem3 = new Problem(3L, "문제3", 6); // Silver V (선호 티어 범위 밖)
        problem3.addTag(tag1);
        problemRepository.save(problem3);

        problem4 = new Problem(4L, "문제4",  4); // Bronze II
        problem4.addTag(tag3);
        problemRepository.save(problem4);
    }

    @Test
    @DisplayName("구독자의 선호 티어와 태그에 맞는 문제 조회 테스트")
    void findProblemsBySubscriberPreferences() {
        // when
        List<Problem> problems = problemRepository.findProblemsBySubscriberPreferences(
                subscriber.getTierPreference().getMinTier(),
                subscriber.getTierPreference().getMaxTier(),
                subscriber.getId()
        );

        // then
        assertThat(problems).hasSize(2); // Bronze V와 Bronze III 문제만 포함
        assertThat(problems).extracting("problemId")
                .containsExactlyInAnyOrder(1L, 2L);

        // 각 문제의 태그 확인
        for (Problem problem : problems) {
            assertThat(problem.getProblemTags()).isNotEmpty();

            assertThat(problem.getProblemTags().stream()
                    .map(pt -> pt.getTag().getDisplayName())
                    .collect(Collectors.toList()))
                    .containsAnyOf("구현", "그리디");
        }
    }

    @Test
    @DisplayName("구독자의 선호 태그만으로 문제 조회 테스트")
    void findByTagPreferences() {
        // when
        List<Problem> problems = problemRepository.findByTagPreferences(subscriber.getId());

        // then
        assertThat(problems).hasSize(3); // 구현 태그와 그리디 태그를 가진 문제들
        assertThat(problems).extracting("problemId")
                .containsExactlyInAnyOrder(1L, 2L, 3L);

        // 각 문제의 태그 확인
        for (Problem problem : problems) {
            assertThat(problem.getProblemTags()).isNotEmpty();

            assertThat(problem.getProblemTags().stream()
                    .map(pt -> pt.getTag().getDisplayName())
                    .collect(Collectors.toList()))
                    .containsAnyOf("구현", "그리디");
        }
    }

    @Test
    @DisplayName("티어 범위로 문제 조회 테스트")
    void findByLevelBetween() {
        // when
        List<Problem> problems = problemRepository.findByLevelBetween(1, 5);

        // then
        assertThat(problems).hasSize(3); // Bronze V ~ Bronze I 범위 내 문제들
        assertThat(problems).extracting("problemId")
                .containsExactlyInAnyOrder(1L, 2L, 4L);

        // 각 문제의 레벨 확인
        for (Problem problem : problems) {
            assertThat(problem.getLevel()).isBetween(1, 5);
        }
    }

    @Test
    @DisplayName("모든 경우의 수 확인 테스트")
    void findProblemsWithDifferentConditions() {
        // 1. 티어와 태그 모두 일치하는 경우
        List<Problem> bothMatch = problemRepository.findProblemsBySubscriberPreferences(1, 5, subscriber.getId());
        assertThat(bothMatch).extracting("problemId").containsExactlyInAnyOrder(1L, 2L);

        // 2. 티어만 일치하는 경우 (수학 태그는 선호 태그에 없음)
        List<Problem> onlyTierMatch = problemRepository.findByLevelBetween(1, 5);
        assertThat(onlyTierMatch).extracting("problemId").containsExactlyInAnyOrder(1L, 2L, 4L);

        // 3. 태그만 일치하는 경우 (Silver V는 선호 티어 범위 밖)
        List<Problem> onlyTagMatch = problemRepository.findByTagPreferences(subscriber.getId());
        assertThat(onlyTagMatch).extracting("problemId").containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}