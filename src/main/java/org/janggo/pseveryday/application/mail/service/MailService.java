package org.janggo.pseveryday.application.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.domain.problem.entity.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail-username}")
    private String sender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerifyMail(String email, String verificationCode) {
        Map<String, Object> variables = Map.of(
                "email", email,
                "verificationCode", verificationCode
        );
        sendMail("📌 PS Everyday - 이메일 인증 코드", email, "mail/verification-mail", variables);
    }

    @Async
    public void sendProblemMail(String email, Problem problem) {
        Map<String, Object> variables = Map.of(
                "email", email,
                "baseUrl", baseUrl,
                "problem", problem,
                "link", "https://www.acmicpc.net/problem/" + problem.getProblemId()
        );
        sendMail("🎯 오늘의 알고리즘 문제", email, "mail/problem-mail", variables);
    }


    public void sendGreetingMail(String email) {
        Map<String, Object> variables = Map.of("email", email, "baseUrl", baseUrl);
        sendMail("🎉 PS Everyday 구독을 환영합니다!", email, "mail/welcome-mail", variables);
    }

    private void sendMail(String subject, String email, String templateName, Map<String, Object> variables) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject(subject);
            helper.setTo(email);
            helper.setFrom(sender);

            Context context = new Context();
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
        }
    }

}
