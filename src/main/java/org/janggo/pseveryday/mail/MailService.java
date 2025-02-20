package org.janggo.pseveryday.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
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

    @Value("${spring.mail.username}")
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
}
