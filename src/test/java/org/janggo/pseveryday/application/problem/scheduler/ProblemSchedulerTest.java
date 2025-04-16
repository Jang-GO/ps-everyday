package org.janggo.pseveryday.application.problem.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.util.TestSubscriberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProblemSchedulerTest {

    @Autowired
    private TestSubscriberGenerator testSubscriberGenerator;

    @Autowired
    private ProblemScheduler problemScheduler;

    @Autowired
    private MailService mailService;

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 1000})
    @DisplayName("사용자 수에 따른 문제 선별 및 발송 시간 측정")
    void measureExecutionTime(int subscriberCount) {
        testSubscriberGenerator.generateTestSubscribers(subscriberCount);

//        doNothing().when(mailService).sendProblemMail(anyString(), any());

        long start = System.currentTimeMillis();
        problemScheduler.scheduleRandomProblemMail();
        long end = System.currentTimeMillis();

        log.info("{}명 - 실행 시간: {}ms", subscriberCount, (end - start));
    }



//    @TestConfiguration
//    static class MockMailServiceConfig {
//        @Bean
//        @Primary // 기존 Bean 대신 이 Bean을 우선적으로 사용하게 함
//        public MailService mailService() {
//            return mock(MailService.class);
//        }
//    }
}
