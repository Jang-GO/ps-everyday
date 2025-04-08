package org.janggo.pseveryday.mail;

import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.problem.solvedac.SolvedAcService;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class MailServiceTest {

    @Autowired
    private MailService mailService;
    @Autowired
    private SolvedAcService solvedAcService;

    @Test
    void testSendProblemMail() {
        // 테스트용 데이터 준비
        String email = "jang8195@naver.com"; // 자신의 이메일 주소 입력
        SolvedAcResponse.Problem problem = solvedAcService.getRandomProblem();
        // 메일 전송 테스트
        mailService.sendProblemMail(email, problem);

        log.info("[성공] To : {} , 문제: {}", email, problem.getTitleKo());
    }
}