package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {
    private final ProblemRepository problemRepository;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for(Subscriber subscriber: subscribers){
            List<Problem> problems;

            // 선호 티어와 태그 정보 확인
            boolean hasTierPreference = subscriber.getTierPreference() != null;
            boolean hasTagPreference = !subscriber.getTagPreferences().isEmpty();

            log.info("{}의 선호 태그 : {}", subscriber.getEmail(), subscriber.getTagPreferenceNames());

            problems = filterProblems(subscriber, hasTierPreference, hasTagPreference);

            if (problems.isEmpty()) {
                log.warn("구독자 {}에게 추천할 문제가 없습니다.", subscriber.getEmail());
                continue;
            }

            Problem randomProblem = problems.get(new Random().nextInt(problems.size()));

            log.info("구독자 {}에게 문제 추천: {} (Level: {}, Tags: {})",
                    subscriber.getEmail(),
                    randomProblem.getTitleKo(),
                    randomProblem.getLevel(),
                    randomProblem.getProblemTags().stream()
                            .map(pt -> pt.getTag().getDisplayName())
                            .collect(Collectors.joining(", "))
            );

            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
        }
    }

    private List<Problem> filterProblems(Subscriber subscriber, boolean hasTierPreference, boolean hasTagPreference) {
        List<Problem> problems;
        if (hasTierPreference && hasTagPreference) {
            // 선호 티어와 태그 모두 있는 경우
            problems = problemRepository.findProblemsBySubscriberPreferences(
                    subscriber.getTierPreference().getMinTier(),
                    subscriber.getTierPreference().getMaxTier(),
                    subscriber.getId()
            );
        } else if (hasTierPreference) {
            // 선호 티어만 있는 경우
            problems = problemRepository.findByLevelBetween(
                    subscriber.getTierPreference().getMinTier(),
                    subscriber.getTierPreference().getMaxTier()
            );
        } else if (hasTagPreference) {
            // 선호 태그만 있는 경우
            problems = problemRepository.findByTagPreferences(subscriber.getId());
        } else {
            // 둘 다 없는 경우
            problems = problemRepository.findAll();
        }
        return problems;
    }
}