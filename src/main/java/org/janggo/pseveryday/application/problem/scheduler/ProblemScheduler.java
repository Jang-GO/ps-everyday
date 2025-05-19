package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.application.problem.RecommendationService; // 변경된 RecommendationService 사용
import org.janggo.pseveryday.domain.problem.entity.Problem;
// import org.janggo.pseveryday.domain.problem.repository.ProblemRepository; // ProblemRepository 직접 의존성 제거 (RecommendationService로 이동)
import org.janggo.pseveryday.domain.recommendation.dto.RecommendationProjection;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.RecommendationRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager; // 디버깅용

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;
    private final RecommendationService recommendationService; // RecommendationService 의존성 유지
    private final RecommendationRepository recommendationRepository; // 과거 추천 기록 조회용

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    // @Scheduled(cron = "*/30 * * * * *", zone = "Asia/Seoul")
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        List<Recommendation> recommendationsToSave = new ArrayList<>();
        Random random = new Random();
        int successCount = 0;
        int failCount = 0;

        log.info("문제 추천 스케줄러 시작. 총 {}명의 구독자 처리 예정.", subscribers.size());

        Map<Long, Set<Long>> allSubscribersRecommendedProblemIds = loadRecommendationsToMem();

        for (Subscriber subscriber : subscribers) {
            try {
                log.debug("구독자 {} 처리 시작. 현재 트랜잭션 활성 여부: {}", subscriber.getEmail(), TransactionSynchronizationManager.isActualTransactionActive());

                Set<Long> recommendedProblemIdsForThisSubscriber = allSubscribersRecommendedProblemIds.getOrDefault(subscriber.getId(), Collections.emptySet());
                log.debug("구독자 {}에게 이미 추천된 문제 ID 개수 (메모리): {}", subscriber.getEmail(), recommendedProblemIdsForThisSubscriber.size());

                // 2. 선호에 맞는 문제 후보 목록 조회 (RecommendationService를 통해 호출)
                List<Problem> problems = recommendationService.filterProblems(subscriber); // 변경된 부분

                // 3. 추천된 문제 제외
                List<Problem> problemsToRecommend = problems.stream()
                        .filter(p -> !recommendedProblemIdsForThisSubscriber.contains(p.getProblemId())) // 수정: allSubscribersRecommendedProblemIds 직접 참조 대신, 해당 구독자의 추천 기록 사용
                        .toList();

                if (problemsToRecommend.isEmpty()) {
                    log.warn("구독자 {}에게 추천할 새로운 문제가 없습니다. (선호도 필터링 후: {}, 추천 제외 후: {})",
                            subscriber.getEmail(), problems.size(), problemsToRecommend.size());
                    continue;
                }

                log.info("{}의 선호 태그 : {}", subscriber.getEmail(), subscriber.getTagPreferenceNames());

                Problem randomProblem = problemsToRecommend.get(random.nextInt(problemsToRecommend.size()));

                log.info("구독자 {}에게 문제 추천 시도: {} (Level: {}, Tags: {})",
                        subscriber.getEmail(),
                        randomProblem.getTitleKo(),
                        randomProblem.getLevel(),
                        randomProblem.getProblemTags().stream()
                                .map(pt -> pt.getTag().getDisplayName())
                                .collect(Collectors.joining(", "))
                );

                mailService.sendProblemMail(subscriber.getEmail(), randomProblem);

                Recommendation recommendation = new Recommendation(subscriber, randomProblem);
                recommendationsToSave.add(recommendation);
                successCount++;
                log.debug("구독자 {} 추천 정보 리스트에 추가 완료.", subscriber.getEmail());

            } catch (Exception e) {
                log.error("구독자 {} 처리 중 오류 발생: {}", subscriber.getEmail(), e.getMessage(), e);
                failCount++;
            }
        }

        if (!recommendationsToSave.isEmpty()) {
            try {
                recommendationService.saveRecommendations(recommendationsToSave);
            } catch (Exception e) {
                log.error("추천 기록 벌크 삽입 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        log.info("문제 추천 스케줄러 종료. 성공: {}, 실패: {}, 저장 시도된 추천 수: {}", successCount, failCount, recommendationsToSave.size());
    }

    private Map<Long, Set<Long>> loadRecommendationsToMem() {
        Map<Long, Set<Long>> recommendedMap = new HashMap<>();
        List<RecommendationProjection> projections = recommendationRepository.findAllSubscriberProblemPairs();

        for (RecommendationProjection projection : projections) {
            if (projection.getSubscriberId() != null && projection.getProblemId() != null) {
                recommendedMap
                        .computeIfAbsent(projection.getSubscriberId(), k -> new HashSet<>())
                        .add(projection.getProblemId());
            }
        }
        log.info("모든 구독자의 과거 추천 기록 (프로젝션) {}건 로드 완료 ({}명의 구독자 정보 포함).",
                projections.size(), recommendedMap.size());
        return recommendedMap;
    }
}
//import java.util.concurrent.CompletableFuture; // CompletableFuture 추가
//import java.util.concurrent.ExecutorService; // ExecutorService 추가
//import java.util.concurrent.atomic.AtomicInteger; // AtomicInteger 추가
//import java.util.Collections; // Collections.synchronizedList 사용 위함
//
//@Component
//@Slf4j
////@RequiredArgsConstructor
//public class ProblemScheduler {
//    private final MailService mailService;
//    private final SubscriberRepository subscriberRepository;
//    private final RecommendationService recommendationService;
//    private final RecommendationRepository recommendationRepository;
//
//    // 생성자 주입 또는 @Autowired + @Qualifier 로 주입
//    private final ExecutorService subscriberProcessingExecutor;
//
//    public ProblemScheduler(MailService mailService,
//                            SubscriberRepository subscriberRepository,
//                            RecommendationService recommendationService,
//                            RecommendationRepository recommendationRepository,
//                            @Qualifier("subscriberProcessingExecutor") ExecutorService subscriberProcessingExecutor) {
//        this.mailService = mailService;
//        this.subscriberRepository = subscriberRepository;
//        this.recommendationService = recommendationService;
//        this.recommendationRepository = recommendationRepository;
//        this.subscriberProcessingExecutor = subscriberProcessingExecutor;
//    }
//
//
//    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
//    public void scheduleRandomProblemMail() {
//        List<Subscriber> subscribers = subscriberRepository.findAll();
//        // 여러 스레드에서 동시에 접근하므로 동기화된 리스트 사용
//        List<Recommendation> recommendationsToSave = Collections.synchronizedList(new ArrayList<>());
//        Random random = new Random(); // Random은 스레드 안전하므로 공유 가능, 또는 각 스레드에서 생성도 고려
//
//        // 성공/실패 카운트를 위한 Atomic 변수 사용
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        log.info("문제 추천 스케줄러 시작. 총 {}명의 구독자 처리 예정.", subscribers.size());
//
//        Map<Long, Set<Long>> allSubscribersRecommendedProblemIds = loadRecommendationsToMem();
//
//        // CompletableFuture를 사용하여 각 구독자 처리를 병렬로 실행
//        List<CompletableFuture<Void>> futures = subscribers.stream()
//                .map(subscriber -> CompletableFuture.runAsync(() -> {
//                    try {
//                        log.debug("구독자 {} 처리 시작 (Thread: {}). 현재 트랜잭션 활성 여부: {}",
//                                subscriber.getEmail(), Thread.currentThread().getName(),
//                                TransactionSynchronizationManager.isActualTransactionActive());
//
//                        Set<Long> recommendedProblemIdsForThisSubscriber =
//                                allSubscribersRecommendedProblemIds.getOrDefault(subscriber.getId(), Collections.emptySet());
//                        log.debug("구독자 {}에게 이미 추천된 문제 ID 개수 (메모리): {}",
//                                subscriber.getEmail(), recommendedProblemIdsForThisSubscriber.size());
//
//                        List<Problem> problems = recommendationService.filterProblems(subscriber);
//
//                        List<Problem> problemsToRecommend = problems.stream()
//                                .filter(p -> !recommendedProblemIdsForThisSubscriber.contains(p.getProblemId()))
//                                .toList();
//
//                        if (problemsToRecommend.isEmpty()) {
//                            log.warn("구독자 {}에게 추천할 새로운 문제가 없습니다. (선호도 필터링 후: {}, 추천 제외 후: {})",
//                                    subscriber.getEmail(), problems.size(), problemsToRecommend.size());
//                            return; // CompletableFuture 내에서는 continue 대신 return 사용
//                        }
//
//                        log.info("{}의 선호 태그 : {}", subscriber.getEmail(), subscriber.getTagPreferenceNames());
//
//                        // 각 스레드에서 Random 객체를 새로 생성하거나, ThreadLocalRandom 사용 고려
//                        Problem randomProblem = problemsToRecommend.get(new Random().nextInt(problemsToRecommend.size()));
//
//                        log.info("구독자 {}에게 문제 추천 시도: {} (Level: {}, Tags: {}) (Thread: {})",
//                                subscriber.getEmail(),
//                                randomProblem.getTitleKo(),
//                                randomProblem.getLevel(),
//                                randomProblem.getProblemTags().stream()
//                                        .map(pt -> pt.getTag().getDisplayName())
//                                        .collect(Collectors.joining(", ")),
//                                Thread.currentThread().getName()
//                        );
//
//                        // mailService.sendProblemMail은 이미 @Async로 비동기 처리되지만,
//                        // 여기서 호출하는 것 자체는 subscriberProcessingExecutor의 스레드에서 실행됨.
//                        mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
//
//                        Recommendation recommendation = new Recommendation(subscriber, randomProblem);
//                        recommendationsToSave.add(recommendation); // 동기화된 리스트에 추가
//                        successCount.incrementAndGet(); // Atomic 변수 증가
//                        log.debug("구독자 {} 추천 정보 리스트에 추가 완료.", subscriber.getEmail());
//
//                    } catch (Exception e) {
//                        log.error("구독자 {} 처리 중 오류 발생 (Thread: {}): {}",
//                                subscriber.getEmail(), Thread.currentThread().getName(), e.getMessage(), e);
//                        failCount.incrementAndGet(); // Atomic 변수 증가
//                    }
//                }, subscriberProcessingExecutor)) // 커스텀 ExecutorService 사용
//                .toList();
//
//        // 모든 CompletableFuture 작업이 완료될 때까지 대기
//        try {
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//            log.info("모든 구독자 처리 작업 완료 (futures joined).");
//        } catch (Exception e) {
//            log.error("구독자 병렬 처리 중 예외 발생 (join 단계): {}", e.getMessage(), e);
//        }
//
//
//        if (!recommendationsToSave.isEmpty()) {
//            try {
//                // saveRecommendations는 트랜잭션이므로, 모든 병렬 작업이 끝난 후 한번에 호출
//                recommendationService.saveRecommendations(new ArrayList<>(recommendationsToSave)); // 동기화된 리스트의 복사본 전달
//                log.info("추천 기록 벌크 삽입 시도: {} 건", recommendationsToSave.size());
//            } catch (Exception e) {
//                log.error("추천 기록 벌크 삽입 중 오류 발생: {}", e.getMessage(), e);
//            }
//        }
//
//        log.info("문제 추천 스케줄러 종료. 성공: {}, 실패: {}, 저장 시도된 추천 수: {}",
//                successCount.get(), failCount.get(), recommendationsToSave.size());
//    }
//
//    private Map<Long, Set<Long>> loadRecommendationsToMem() {
//        // 이 부분은 스케줄러 시작 시 한 번만 로드되므로 병렬 처리 대상이 아님
//        Map<Long, Set<Long>> recommendedMap = new HashMap<>();
//        List<RecommendationProjection> projections = recommendationRepository.findAllSubscriberProblemPairs();
//
//        for (RecommendationProjection projection : projections) {
//            if (projection.getSubscriberId() != null && projection.getProblemId() != null) {
//                recommendedMap
//                        .computeIfAbsent(projection.getSubscriberId(), k -> new HashSet<>())
//                        .add(projection.getProblemId());
//            }
//        }
//        log.info("모든 구독자의 과거 추천 기록 (프로젝션) {}건 로드 완료 ({}명의 구독자 정보 포함).",
//                projections.size(), recommendedMap.size());
//        return recommendedMap;
//    }
//}
