package org.janggo.pseveryday.schedule;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.problem.SolvedAcService;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.subscriber.Subscriber;
import org.janggo.pseveryday.subscriber.SubscriberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemScheduler {

    private final SolvedAcService solvedAcService;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "0 0 8 * * *") // 매일 오전 8시 실행
    public void scheduleProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        SolvedAcResponse.Problem randomProblem = solvedAcService.getRandomProblem();

        for(Subscriber subscriber: subscribers){
            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
        }
    }
}