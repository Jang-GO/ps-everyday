package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.mail.MailService;
import org.janggo.pseveryday.mail.VerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SubscriberController {

    private final VerificationService verificationService;
    private final MailService mailService;

    // 화면 조회
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("showVerificationForm", false); // 초기에는 인증 코드 입력 양식을 숨김
        return "home";
    }

    // 이메일 등록 후 인증 코드 전송
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email, Model model) {
        String verificationCode = verificationService.generationVerificationCode(email);
        mailService.sendVerifyMail(email, verificationCode);

        model.addAttribute("showVerificationForm", true);
        model.addAttribute("email", email);
        return "home";
    }

    // 인증 코드 확인
    @PostMapping("/verify")
    public String verify(@RequestParam String email, @RequestParam String verificationCode, Model model) {
        boolean isVerified = verificationService.verifyCode(email, verificationCode);

        if (isVerified) {
            model.addAttribute("message", "인증 완료되었습니다!");
            model.addAttribute("showVerificationForm", false); // 인증 성공하면 입력 폼 숨김
        } else {
            model.addAttribute("message", "인증 코드가 잘못되었습니다. 다시 시도해 주세요.");
            model.addAttribute("showVerificationForm", true); // 인증 실패해도 계속 입력 폼 표시
        }

        model.addAttribute("email", email); // 이메일 정보를 유지하여 계속 폼에 사용
        return "home";
    }
}
