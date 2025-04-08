package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.problem.dto.ProblemLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscribeService subscribeService;

    // 화면 조회
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("showVerificationForm", false);
        model.addAttribute("showPreferenceForm", false);
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", Arrays.asList("DP", "그리디", "구현", "브루트포스", "정렬", "이분탐색", "BFS", "DFS", "다익스트라", "플로이드워셜"));
        return "home";
    }

    // 이메일 등록 후 인증 코드 전송
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("email") String email, Model model) {
        subscribeService.sendVerificationCode(email);

        model.addAttribute("showVerificationForm", true);
        model.addAttribute("showPreferenceForm", false);
        model.addAttribute("email", email);
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", Arrays.asList("DP", "그리디", "구현", "브루트포스", "정렬", "이분탐색", "BFS", "DFS", "다익스트라", "플로이드워셜"));
        return "home";
    }

    // 인증 코드 확인
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
        model.addAttribute("availableTags", Arrays.asList("DP", "그리디", "구현", "브루트포스", "정렬", "이분탐색", "BFS", "DFS", "다익스트라", "플로이드워셜"));
        return "home";
    }

    // 선호도 저장
    @PostMapping("/save-preferences")
    public String savePreferences(@RequestParam("email") String email,
                                  @RequestParam(value = "minTier", required = false) Integer minTier,
                                  @RequestParam(value = "maxTier", required = false) Integer maxTier,
                                  @RequestParam(value = "tags", required = false) List<String> tags,
                                  Model model) {
        // minTier와 maxTier가 null이면 모든 레벨을 수신한다는 의미
        subscribeService.subscribe(email, minTier, maxTier, tags);

        model.addAttribute("message", "구독이 완료되었습니다! 매일 8시에 알고리즘 문제를 보내드립니다.");
        model.addAttribute("showVerificationForm", false);
        model.addAttribute("showPreferenceForm", false);
        return "home";
    }

    // 구독 취소 페이지
    @GetMapping("/unsubscribe")
    public String unsubscribePage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "mail/unsubscribe";
    }

    // 구독 취소 처리
    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam("email") String email, Model model) {
        boolean unsubscribed = subscribeService.unsubscribe(email);

        if (unsubscribed) {
            model.addAttribute("message", "구독이 성공적으로 취소되었습니다.");
        } else {
            model.addAttribute("message", "구독 정보를 찾을 수 없습니다.");
        }

        return "mail/unsubscribe-result";
    }
}