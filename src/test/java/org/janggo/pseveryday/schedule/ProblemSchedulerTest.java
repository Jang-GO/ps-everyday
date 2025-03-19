package org.janggo.pseveryday.schedule;

import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.problem.SolvedAcService;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.janggo.pseveryday.subscriber.Subscriber;
import org.janggo.pseveryday.subscriber.SubscriberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProblemSchedulerTest {

    @Mock
    SolvedAcService solvedAcService;
    @Mock
    MailService mailService;
    @Mock
    SubscriberRepository subscriberRepository;
    @InjectMocks
    ProblemScheduler problemScheduler;

    @DisplayName("")
    @Test
    void test() {
        // Given
        Subscriber testUser = new Subscriber("testEmail");
        SolvedAcResponse.Problem mockProblem = new SolvedAcResponse.Problem();
        mockProblem.setProblemId(12345);
        mockProblem.setTitleKo("테스트용 제목");
        mockProblem.setLevel(1);

        Mockito.when(solvedAcService.getRandomProblem()).thenReturn(mockProblem);
        Mockito.when(subscriberRepository.findAll()).thenReturn(List.of(testUser));
        // When
        problemScheduler.scheduleRandomProblemMail();

        // Then
        Mockito.verify(solvedAcService).getRandomProblem();
        Mockito.verify(mailService).sendProblemMail(testUser.getEmail(),mockProblem);
    }
}