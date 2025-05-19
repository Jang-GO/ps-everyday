package org.janggo.pseveryday.application.problem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository; // ProblemRepository 의존성 추가
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.JdbcRecommendationRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber; // Subscriber 의존성 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager; // 디버깅용

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // filterProblems 내부의 log 사용을 위해 추가
public class RecommendationService {
    private final JdbcRecommendationRepository jdbcRecommendationRepository;
    private final ProblemRepository problemRepository; // filterProblems를 위해 의존성 추가

    /**
     * 추천 기록을 벌크로 저장합니다.
     * 이 메서드는 쓰기 트랜잭션 내에서 실행됩니다.
     * @param recommendations 저장할 추천 기록 리스트
     */
    @Transactional // 쓰기 트랜잭션
    public void saveRecommendations(List<Recommendation> recommendations) {
        log.info("추천 기록 {}건 벌크 삽입 시작. 현재 트랜잭션 활성 여부: {}", recommendations.size(), TransactionSynchronizationManager.isActualTransactionActive());
        jdbcRecommendationRepository.bulkInsert(recommendations);
        log.info("총 {}건의 추천 기록이 성공적으로 저장되었습니다.", recommendations.size());
    }

    /**
     * 구독자의 선호도에 맞는 문제 목록을 필터링하여 반환합니다.
     * 이 메서드는 읽기 전용 트랜잭션 내에서 실행됩니다.
     * @param subscriber 구독자 정보
     * @return 필터링된 문제 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public List<Problem> filterProblems(Subscriber subscriber) {
        log.debug("구독자 {}의 문제 필터링 시작. 현재 트랜잭션 활성 여부 (읽기 전용 예상): {}", subscriber.getEmail(), TransactionSynchronizationManager.isActualTransactionActive());
        boolean hasTierPreference = subscriber.getTierPreference() != null;
        boolean hasTagPreference = subscriber.getTagPreferences() != null && !subscriber.getTagPreferences().isEmpty(); // null 체크 강화

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
    }
}
