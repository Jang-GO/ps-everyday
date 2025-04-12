package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.external.solvedac.service.SolvedAcService;
import org.janggo.pseveryday.domain.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {

    private final SolvedAcService solvedAcService;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "* * 8 * * *") // 매일 오전 8시 실행
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        SolvedAcResponse.ProblemItem randomProblem = solvedAcService.getRandomProblem();

        for(Subscriber subscriber: subscribers){
            log.info("구독지 : {}", subscriber.getEmail());
            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
        }
    }
}