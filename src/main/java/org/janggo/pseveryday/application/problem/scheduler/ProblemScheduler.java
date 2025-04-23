package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.RecommendationRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {
    private final ProblemRepository problemRepository;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;
    private final RecommendationRepository recommendationRepository;
    private final Random random = new Random(); // 클래스 레벨에서 Random 객체 생성

    // 매일 오전 9시에 실행 (실제 요구사항에 맞게 조정)
    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    public void scheduleRandomProblemMail() {
        try {
            log.info("문제 추천 메일 스케줄러 시작");
            List<Subscriber> subscribers = getSubscribers();

            if (subscribers.isEmpty()) {
                log.info("구독자가 없습니다.");
                return;
            }

            List<Recommendation> recommendationsToSave = new ArrayList<>();

            for (Subscriber subscriber : subscribers) {
                try {
                    processSubscriber(subscriber, recommendationsToSave);
                } catch (Exception e) {
                    log.error("구독자 {} 처리 중 오류 발생: {}", subscriber.getEmail(), e.getMessage(), e);
                    // 한 구독자 처리 실패해도 다른 구독자는 계속 처리
                }
            }

            saveRecommendations(recommendationsToSave);

        } catch (Exception e) {
            log.error("문제 추천 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    protected List<Subscriber> getSubscribers() {
        return subscriberRepository.findAll();
    }

    private void processSubscriber(Subscriber subscriber, List<Recommendation> recommendationsToSave) {
        List<Problem> problems = getFilteredProblems(subscriber);

        if (problems.isEmpty()) {
            log.warn("구독자 {}에게 추천할 문제가 없습니다.", subscriber.getEmail());
            return;
        }

        Problem randomProblem = problems.get(random.nextInt(problems.size()));

        try {
            sendProblemEmail(subscriber, randomProblem);
            recommendationsToSave.add(new Recommendation(subscriber, randomProblem));
        } catch (Exception e) {
            log.error("구독자 {}에게 메일 전송 실패: {}", subscriber.getEmail(), e.getMessage(), e);
            // 메일 전송이 실패해도 추천 내역은 저장하지 않음
        }
    }

    @Transactional(readOnly = true)
    protected List<Problem> getFilteredProblems(Subscriber subscriber) {
        boolean hasTierPreference = subscriber.getTierPreference() != null;
        boolean hasTagPreference = !subscriber.getTagPreferences().isEmpty();

        log.info("{}의 선호 태그: {}", subscriber.getEmail(), subscriber.getTagPreferenceNames());

        return filterProblems(subscriber, hasTierPreference, hasTagPreference);
    }

    private void sendProblemEmail(Subscriber subscriber, Problem problem) {
        log.info("구독자 {}에게 문제 추천: {} (Level: {}, Tags: {})",
                subscriber.getEmail(),
                problem.getTitleKo(),
                problem.getLevel(),
                problem.getProblemTags().stream()
                        .map(pt -> pt.getTag().getDisplayName())
                        .collect(Collectors.joining(", "))
        );

        mailService.sendProblemMail(subscriber.getEmail(), problem);
    }

    @Transactional
    protected void saveRecommendations(List<Recommendation> recommendations) {
        if (!recommendations.isEmpty()) {
            recommendationRepository.saveAll(recommendations);
            log.info("총 {}건의 추천 기록이 저장되었습니다.", recommendations.size());
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