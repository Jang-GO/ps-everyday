package org.janggo.pseveryday.application.problem.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.mail.service.MailService;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.janggo.pseveryday.domain.problem.repository.ProblemRepository;
import org.janggo.pseveryday.external.solvedac.service.SolvedAcService;
import org.janggo.pseveryday.domain.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProblemScheduler {
    private final ProblemRepository problemRepository;
    private final MailService mailService;
    private final SubscriberRepository subscriberRepository;

    @Scheduled(cron = "*/5 * * * * *") // 매일 오전 8시 실행
    public void scheduleRandomProblemMail() {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for(Subscriber subscriber: subscribers){
            log.info("min, max = {}, {}", subscriber.getTierPreference().getMinTier(), subscriber.getTierPreference().getMaxTier());
            // 구독자의 선호 난이도 범위에 맞는 문제들 조회
            List<Problem> problems = problemRepository.findByLevelBetweenWithTags(
                    subscriber.getTierPreference().getMinTier(),
                    subscriber.getTierPreference().getMaxTier()
            );

            if (problems.isEmpty()) {
                log.warn("구독자 {}에게 추천할 문제가 없습니다.", subscriber.getEmail());
                continue;
            }

            // 랜덤으로 문제 선택
            Problem randomProblem = problems.get(new Random().nextInt(problems.size()));

            log.info("구독자 {}에게 문제 추천: {} (Level : {})", subscriber.getEmail(), randomProblem.getTitleKo(), randomProblem.getLevel());
            mailService.sendProblemMail(subscriber.getEmail(), randomProblem);
        }
    }
}