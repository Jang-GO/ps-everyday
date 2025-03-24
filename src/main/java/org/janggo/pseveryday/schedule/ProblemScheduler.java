package org.janggo.pseveryday.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.problem.SolvedAcService;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.subscriber.Subscriber;
import org.janggo.pseveryday.subscriber.SubscriberRepository;
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

        SolvedAcResponse.Problem randomProblem = solvedAcService.getRandomProblem();

        for(Subscriber subscriber: subscribers){
            log.info("구독지 : {}", subscriber.getEmail());
            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
        }
    }
}