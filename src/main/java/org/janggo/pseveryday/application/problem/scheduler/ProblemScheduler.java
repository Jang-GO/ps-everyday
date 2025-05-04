package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.JdbcRecommendationRepository; // 벌크 인서트 사용
import org.janggo.pseveryday.domain.recommendation.repository.RecommendationRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager; // 디버깅용

import java.util.ArrayList; // 벌크 인서트 위해 사용
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
    private final JdbcRecommendationRepository jdbcRecommendationRepository; // 벌크 인서트 위해 사용
    private final RecommendationRepository recommendationRepository; // ID 조회 위해 사용

    //    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "*/30 * * * * *", zone = "Asia/Seoul")
    // @Transactional 제거: 전체 메소드 트랜잭션 해제
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        List<Recommendation> recommendationsToSave = new ArrayList<>(); // 벌크 저장을 위한 리스트
        Random random = new Random();
        int successCount = 0;
        int failCount = 0;

        log.info("문제 추천 스케줄러 시작. 총 {}명의 구독자 처리 예정.", subscribers.size());

        for (Subscriber subscriber : subscribers) {
            try {
                log.debug("구독자 {} 처리 시작. 현재 트랜잭션 활성 여부: {}", subscriber.getEmail(), TransactionSynchronizationManager.isActualTransactionActive()); // 트랜잭션 상태 확인 (디버깅용)

                // 1. 해당 구독자에게 이미 추천된 문제 ID 목록 조회 (읽기 전용 작업)
                // 이 작업은 별도 트랜잭션이 필요할 수 있으나, 보통 readOnly로 처리 가능
                Set<Long> recommendedProblemIds = recommendationRepository.findRecommendedProblemIdsBySubscriber(subscriber);
                log.debug("구독자 {}에게 이미 추천된 문제 ID 개수: {}", subscriber.getEmail(), recommendedProblemIds.size());

                // 2. 선호에 맞는 문제 후보 목록 조회 (읽기 전용 작업)
                List<Problem> problems = filterProblems(subscriber); // filterProblems는 내부적으로 readOnly 트랜잭션 사용 권장

                // 3. 추천된 문제 제외
                List<Problem> problemsToRecommend = problems.stream()
                        .filter(p -> !recommendedProblemIds.contains(p.getProblemId()))
                        .toList();

                if (problemsToRecommend.isEmpty()) {
                    log.warn("구독자 {}에게 추천할 새로운 문제가 없습니다. (선호도 필터링 후: {}, 추천 제외 후: {})",
                            subscriber.getEmail(), problems.size(), problemsToRecommend.size());
                    continue; // 다음 구독자로 넘어감
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

                // 4. 메일 발송 시도
                mailService.sendProblemMail(subscriber.getEmail(), randomProblem);

                // 5. 메일 발송 성공 시 (예외가 발생하지 않으면) 추천 기록 객체 생성 및 리스트에 추가
                Recommendation recommendation = new Recommendation(subscriber, randomProblem);
                recommendationsToSave.add(recommendation);
                successCount++;
                log.debug("구독자 {} 추천 정보 리스트에 추가 완료.", subscriber.getEmail());

            } catch (Exception e) {
                // 개별 구독자 처리 실패 시 로그 기록 및 다음 구독자로 진행
                log.error("구독자 {} 처리 중 오류 발생: {}", subscriber.getEmail(), e.getMessage(), e);
                failCount++;
                // 실패 시 recommendationsToSave에 추가하지 않음
            }
        }

        // 루프 종료 후, 저장할 추천 기록이 있다면 벌크 삽입 실행
        if (!recommendationsToSave.isEmpty()) {
            try {
                saveRecommendations(recommendationsToSave); // @Transactional 메소드 호출
            } catch (Exception e) {
                log.error("추천 기록 벌크 삽입 중 오류 발생: {}", e.getMessage(), e);
                // 벌크 삽입 실패 시 추가 처리 로직 (예: 개별 저장 시도, 실패 로깅 등)
            }
        }

        log.info("문제 추천 스케줄러 종료. 성공: {}, 실패: {}, 저장 시도된 추천 수: {}", successCount, failCount, recommendationsToSave.size());
    }

    // 벌크 삽입을 위한 메소드, @Transactional 적용
    @Transactional
    protected void saveRecommendations(List<Recommendation> recommendations) {
        log.info("추천 기록 {}건 벌크 삽입 시작. 현재 트랜잭션 활성 여부: {}", recommendations.size(), TransactionSynchronizationManager.isActualTransactionActive()); // 트랜잭션 상태 확인 (디버깅용)
        jdbcRecommendationRepository.bulkInsert(recommendations);
        log.info("총 {}건의 추천 기록이 성공적으로 저장되었습니다.", recommendations.size());
    }

    // filterProblems 메소드는 읽기 전용 트랜잭션으로 유지하는 것이 좋음
    @Transactional(readOnly = true)
    protected List<Problem> filterProblems(Subscriber subscriber) {
        // ... existing code ...
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
            log.warn("구독자 {}가 선호도를 설정하지 않아 모든 문제를 대상으로 합니다. (성능 저하 가능성)", subscriber.getEmail());
            // findAll() 대신 페이징 또는 개수 제한 고려
            return problemRepository.findAll(); // 성능 이슈 주의
        }
        // ... existing code ...
    }
}