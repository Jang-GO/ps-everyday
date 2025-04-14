package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.entity.TierPreference;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProblemRepositoryTest {
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private TagRepository tagRepository;

    @Mock
    private MailService mailService;

    @Test
    @DisplayName("구독자의 선호 티어와 태그에 맞는 문제 조회 테스트")
    void findProblemsBySubscriberPreferences() {
        // given
        // 1. 태그 생성
        Tag tag1 = new Tag("구현", "implementation");
        Tag tag2 = new Tag("그리디", "greedy");
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        // 2. 구독자 생성 및 선호 설정
        Subscriber subscriber = new Subscriber("test@example.com", new TierPreference(1, 5)); // Bronze V ~ Bronze I
        subscriber.addTagPreference(tag1);
        subscriber.addTagPreference(tag2);
        subscriberRepository.save(subscriber);

        // 3. 문제 생성
        Problem problem1 = new Problem(1L, "문제1", 1); // Bronze V
        problem1.addTag(tag1);
        problemRepository.save(problem1);

        Problem problem2 = new Problem(2L, "문제2", 3); // Bronze III
        problem2.addTag(tag2);
        problemRepository.save(problem2);

        Problem problem3 = new Problem(3L, "문제3", 6); // Silver V (선호 티어 범위 밖)
        problem3.addTag(tag1);
        problemRepository.save(problem3);

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
}