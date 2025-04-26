package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.JdbcRecommendationRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {
    private final ProblemRepository problemRepository;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;
    private final JdbcRecommendationRepository jdbcRecommendationRepository;
    private final RecommendationRepository recommendationRepository;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "*/30 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        List<Recommendation> recommendationsToSave = new ArrayList<>();
        Random random = new Random();

        for(Subscriber subscriber: subscribers){
            // 1. 해당 구독자에게 이미 추천된 문제 ID 목록 조회
            Set<Long> recommendedProblemIds = recommendationRepository.findRecommendedProblemIdsBySubscriber(subscriber);
            log.debug("구독자 {}에게 이미 추천된 문제 ID 개수: {}", subscriber.getEmail(), recommendedProblemIds.size());

            // 2. 선호에 맞는 문제 후보 목록 조회
            List<Problem> problems = filterProblems(subscriber);

            // 3. 추천된 문제 제외
            List<Problem> problemsToRecommend = problems.stream()
                    .filter(p -> !recommendedProblemIds.contains(p.getProblemId()))
                    .collect(Collectors.toList());

            if (problemsToRecommend.isEmpty()) {
                // 필터링 후 추천할 문제가 없는 경우 (선호도 맞는 문제가 없거나, 모두 이미 추천된 경우)
                log.warn("구독자 {}에게 추천할 새로운 문제가 없습니다. (선호도 필터링 후: {}, 추천 제외 후: {})",
                        subscriber.getEmail(), problems.size(), problemsToRecommend.size());
                continue;
            }

            log.info("{}의 선호 태그 : {}", subscriber.getEmail(), subscriber.getTagPreferenceNames());

            Problem randomProblem = problemsToRecommend.get(random.nextInt(problemsToRecommend.size()));

            log.info("구독자 {}에게 문제 추천: {} (Level: {}, Tags: {})",
                    subscriber.getEmail(),
                    randomProblem.getTitleKo(),
                    randomProblem.getLevel(),
                    randomProblem.getProblemTags().stream()
                            .map(pt -> pt.getTag().getDisplayName())
                            .collect(Collectors.joining(", "))
            );

            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);

            // ⬇️ 벌크 저장을 위한 객체 생성
            Recommendation recommendation = new Recommendation(subscriber, randomProblem);
            recommendationsToSave.add(recommendation);
        }

        if(!recommendationsToSave.isEmpty()){
            saveRecommendations(recommendationsToSave);
        }
    }

    protected void saveRecommendations(List<Recommendation> recommendations) {
        jdbcRecommendationRepository.bulkInsert(recommendations);
        log.info("총 {}건의 추천 기록이 저장되었습니다.", recommendations.size());
    }

    private List<Problem> filterProblems(Subscriber subscriber) {
        boolean hasTierPreference = subscriber.getTierPreference() != null;
        boolean hasTagPreference = !subscriber.getTagPreferences().isEmpty();

        if (hasTierPreference && hasTagPreference) {
            return problemRepository.findProblemsBySubscriberPreferences(
                    subscriber.getTierPreference().getMinTier(),
                    subscriber.getTierPreference().getMaxTier(),
                    subscriber.getId()
            );
        } else if (hasTierPreference) {
            return problemRepository.findByLevelBetween(
                    subscriber.getTierPreference().getMinTier(),
                    subscriber.getTierPreference().getMaxTier()
            );
        } else if (hasTagPreference) {
            return problemRepository.findByTagPreferences(subscriber.getId());
        } else {
            return problemRepository.findAll(); // 필요시 findAll에도 추천 제외 로직 추가 고려
        }
    }
}