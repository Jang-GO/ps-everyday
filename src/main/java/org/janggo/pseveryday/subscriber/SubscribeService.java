package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.mail.VerificationService;
import org.janggo.pseveryday.subscriber.entity.Subscriber;
import org.janggo.pseveryday.subscriber.entity.TierPreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscribeService {

    private final VerificationService verificationService;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;

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
    public void subscribe(String email, int minTier, int maxTier, List<String> tags) {
        Subscriber subscriber = new Subscriber(email, new TierPreference(minTier, maxTier));
        if (tags != null) {
            tags.forEach(subscriber::addTagPreference);
        }
        subscriberRepository.save(subscriber);
    }

    public boolean unsubscribe(String email) {
        if (subscriberRepository.existsByEmail(email)) {
            subscriberRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }
}
