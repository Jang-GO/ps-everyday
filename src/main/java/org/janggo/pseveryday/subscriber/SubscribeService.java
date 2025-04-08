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
    public void subscribe(String email, int minTier, int maxTier, List<String> tagNames) {
        Subscriber subscriber = new Subscriber(email, new TierPreference(minTier, maxTier));
        if (tagNames != null) {
            List<Tag> tags = tagNames.stream()
                    .map(tagRepository::findByDisplayName)
                    .collect(Collectors.toList());
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
