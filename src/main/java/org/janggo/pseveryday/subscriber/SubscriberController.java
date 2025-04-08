package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.problem.dto.ProblemLevel;
import org.janggo.pseveryday.problem.entity.Tag;
import org.janggo.pseveryday.problem.repository.TagRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscribeService subscribeService;
    private final TagRepository tagRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("showVerificationForm", false);
        model.addAttribute("showPreferenceForm", false);
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", tagRepository.findAll());
        return "home";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("email") String email, Model model) {
        subscribeService.sendVerificationCode(email);

        model.addAttribute("showVerificationForm", true);
        model.addAttribute("showPreferenceForm", false);
        model.addAttribute("email", email);
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", tagRepository.findAll());
        return "home";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email,
                         @RequestParam("verificationCode") String verificationCode,
                         Model model) {
        boolean isVerified = subscribeService.verifyCode(email, verificationCode);

        if (isVerified) {
            model.addAttribute("message", "인증이 완료되었습니다! 선호하는 태그와 티어를 선택해주세요.");
            model.addAttribute("showVerificationForm", false);
            model.addAttribute("showPreferenceForm", true);
        } else {
            model.addAttribute("message", "인증 코드가 잘못되었습니다. 다시 시도해 주세요.");
            model.addAttribute("showVerificationForm", true);
            model.addAttribute("showPreferenceForm", false);
        }

        model.addAttribute("email", email);
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", tagRepository.findAll());
        return "home";
    }

    @PostMapping("/save-preferences")
    public String savePreferences(@RequestParam("email") String email,
                                  @RequestParam(value = "minTier", required = false) Integer minTier,
                                  @RequestParam(value = "maxTier", required = false) Integer maxTier,
                                  @RequestParam(value = "tags", required = false) List<String> tagNames,
                                  Model model) {
        subscribeService.subscribe(email, minTier != null ? minTier : 0, maxTier != null ? maxTier : 30, tagNames);

        model.addAttribute("message", "구독이 완료되었습니다! 매일 8시에 알고리즘 문제를 보내드립니다.");
        model.addAttribute("showVerificationForm", false);
        model.addAttribute("showPreferenceForm", false);
        return "home";
    }

    @GetMapping("/unsubscribe")
    public String unsubscribePage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "mail/unsubscribe";
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam("email") String email, Model model) {
        boolean unsubscribed = subscribeService.unsubscribe(email);

        if (unsubscribed) {
            model.addAttribute("message", "구독이 성공적으로 취소되었습니다.");
        } else {
            model.addAttribute("message", "구독 정보를 찾을 수 없습니다.");
        }

        model.addAttribute("showVerificationForm", false);
        model.addAttribute("showPreferenceForm", false);
        return "home";
    }
}