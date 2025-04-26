package org.janggo.pseveryday.application.subscriber.service;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.recommendation.repository.RecommendationRepository;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.application.mail.service.VerificationService;
import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.janggo.pseveryday.domain.problem.repository.TagRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.entity.TierPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeService {

    private final VerificationService verificationService;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;
    private final TagRepository tagRepository;
    private final RecommendationRepository recommendationRepository;

    /**
     * 이메일 등록 및 인증 코드 전송
     */
    public void sendVerificationCode(String email) {
        String verificationCode = verificationService.generationVerificationCode(email);
        mailService.sendVerifyMail(email, verificationCode);
    }

    /**
     * 인증 코드 확인
     */
    public boolean verifyCode(String email, String verificationCode) {
        return verificationService.verifyCode(email, verificationCode);
    }

    /**
     * 선호도 저장
     */
    @Transactional
    public void subscribe(String email, Integer minTier, Integer maxTier, List<Long> tagIds) {
        if (minTier == null) minTier = 1;
        if (maxTier == null) maxTier = 30;

        Optional<Subscriber> existingSubscriber = subscriberRepository.findByEmail(email);
        Subscriber subscriber;

        if (existingSubscriber.isPresent()) {
            // 기존 구독자 정보 업데이트
            subscriber = existingSubscriber.get();
            subscriber.updateTierPreference(new TierPreference(minTier, maxTier));
            subscriber.clearTagPreferences(); // 기존 태그 제거
        } else {
            // 새 구독자 생성
            subscriber = new Subscriber(email, new TierPreference(minTier, maxTier));
        }

        // 태그 추가
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);

            if (!tags.isEmpty()) {
                tags.forEach(subscriber::addTagPreference);
            }
        }
        subscriberRepository.save(subscriber);
        mailService.sendGreetingMail(email);
    }

    @Transactional
    public boolean unsubscribe(String email) {
        if (subscriberRepository.existsByEmail(email)) {
            subscriberRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    public List<Recommendation> getRecommendationsByEmail(String email) {
        Optional<Subscriber> subscriberOptional = subscriberRepository.findByEmail(email);
        if (subscriberOptional.isPresent()) {
            return recommendationRepository.findBySubscriberWithProblem(subscriberOptional.get());
        } else {
            // 구독자가 존재하지 않으면 빈 리스트 반환
            return Collections.emptyList();
        }
    }

    public Page<Recommendation> getRecommendationsByEmail(String email, Pageable pageable) {
        // 이메일로 Subscriber 찾기 (예시)
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
        // 해당 Subscriber의 Recommendation 목록을 페이징하여 조회
        return recommendationRepository.findBySubscriberOrderByRecommendedAtDesc(subscriber, pageable);
        // 또는 필요에 따라 다른 조회 메서드 사용
    }
}
