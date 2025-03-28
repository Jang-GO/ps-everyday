package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.mail.VerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 인증 코드 확인 및 구독자 등록
     */
    public boolean verifyAndSubscribe(String email, String verificationCode) {
        boolean isVerified = verificationService.verifyCode(email, verificationCode);

        if (isVerified) {
            registerSubscriber(email);
            mailService.sendGreetingMail(email);
        }

        return isVerified;
    }

    public boolean unsubscribe(String email){
        if(subscriberRepository.existsByEmail(email)){
            subscriberRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    /**
     * 구독자 등록 (이미 존재하지 않을 경우에만)
     */
    private void registerSubscriber(String email) {
        if (!subscriberRepository.existsByEmail(email)) {
            Subscriber subscriber = new Subscriber(email);
            subscriberRepository.save(subscriber);
        }
    }

}
