package org.janggo.pseveryday.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.problem.dto.SolvedAcResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail-username}")
    private String sender;

    public void sendVerifyMail(String email, String verificationCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setSubject("📌 PS Everyday - 이메일 인증 코드");
            helper.setTo(email);
            helper.setFrom(sender);

            // 타임리프 템플릿에서 이메일 본문 생성
            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("verificationCode", verificationCode);
            String htmlContent = templateEngine.process("mail/verification-mail", context);

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
        }
    }

    public void sendProblemMail(String email, SolvedAcResponse.Problem problem) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setSubject("🎯 오늘의 알고리즘 문제");
            helper.setTo(email);
            helper.setFrom(sender);

            String link = "https://www.acmicpc.net/problem/" + problem.getProblemId();

            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("problem", problem);
            context.setVariable("link", link);
            String htmlContent = templateEngine.process("mail/problem-mail", context);

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
        }
    }

    public void sendGreetingMail(String email) {
        // JavaMailSender로 이메일 객체 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true: 멀티파트 지원
            helper.setSubject("🎉 PS Everyday 구독을 환영합니다!");
            helper.setTo(email);
            helper.setFrom(sender);

            // Thymeleaf Context 설정
            Context context = new Context();
            context.setVariable("email", email); // HTML에서 사용할 email 변수 전달

            // 템플릿 렌더링 (subscription-mail.html)
            String htmlContent = templateEngine.process("mail/welcome-mail", context);

            helper.setText(htmlContent, true); // true: HTML 콘텐츠로 설정
            javaMailSender.send(mimeMessage); // 이메일 전송
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
        }
    }
}
