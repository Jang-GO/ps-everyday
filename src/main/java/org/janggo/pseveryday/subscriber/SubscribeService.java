package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.mail.VerificationService;
import org.janggo.pseveryday.problem.entity.Tag;
import org.janggo.pseveryday.problem.repository.TagRepository;
import org.janggo.pseveryday.subscriber.entity.Subscriber;
import org.janggo.pseveryday.subscriber.entity.TierPreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscribeService {

    private final VerificationService verificationService;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;
    private final TagRepository tagRepository;

    /**
     * 이메일 등록 및 인증 코드 전송
     */
    public String sendVerificationCode(String email) {
        String verificationCode = verificationService.generationVerificationCode(email);
        mailService.sendVerifyMail(email, verificationCode);
        return verificationCode;
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

    public boolean unsubscribe(String email) {
        if (subscriberRepository.existsByEmail(email)) {
            subscriberRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }
}
