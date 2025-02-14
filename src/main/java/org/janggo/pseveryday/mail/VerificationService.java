package org.janggo.pseveryday.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int CODE_EXPIRE_SECONDS = 300; // 인증번호 유효 시간 (5분)
    private static final SecureRandom secureRandom = new SecureRandom();

    public String generationVerificationCode(String email){
        String verificationCode = String.format("%06d",generateVerificationCode());
        verificationCodes.put(email, verificationCode);

        scheduler.schedule(() -> verificationCodes.remove(verificationCode),
                CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return verificationCode;
    }

    private int generateVerificationCode() {
        return secureRandom.nextInt(1000000); // 6자리 숫자 생성

    }
}
