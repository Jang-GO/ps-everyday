package org.janggo.pseveryday.presentation;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.application.subscriber.service.SubscribeService;
import org.janggo.pseveryday.domain.problem.dto.ProblemLevel;
import org.janggo.pseveryday.domain.problem.repository.TagRepository;
import org.janggo.pseveryday.domain.recommendation.dto.RecommendationDto;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @ResponseBody
    public Map<String, Object> verify(
            @RequestParam("email") String email,
            @RequestParam("verificationCode") String verificationCode) {

        boolean isVerified = subscribeService.verifyCode(email, verificationCode);

        Map<String, Object> response = new HashMap<>();
        response.put("success", isVerified);
        response.put("message", isVerified ?
                "인증이 완료되었습니다! 선호하는 태그와 티어를 선택해주세요." :
                "인증 코드가 잘못되었습니다. 다시 시도해 주세요.");

        return response;
    }


    @PostMapping("/save-preferences")
    public String savePreferences(@RequestParam("email") String email,
                                  @RequestParam(value = "minTier", required = false) Integer minTier,
                                  @RequestParam(value = "maxTier", required = false) Integer maxTier,
                                  @RequestParam(value = "tags", required = false) List<Long> tagIds,
                                  RedirectAttributes redirectAttributes) {
        try {
            subscribeService.subscribe(email, minTier , maxTier , tagIds);
            redirectAttributes.addFlashAttribute("message", "구독이 완료되었습니다! 매일 8시에 알고리즘 문제를 보내드립니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "구독 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
        }
        return "redirect:/";
    }

    @GetMapping("/unsubscribe")
    public String unsubscribePage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "mail/unsubscribe";
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        boolean unsubscribed = subscribeService.unsubscribe(email);

        if (unsubscribed) {
            redirectAttributes.addFlashAttribute("message", "구독이 성공적으로 취소되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("message", "구독 정보를 찾을 수 없습니다.");
        }

        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam("email") String email, Model model,
                            @PageableDefault Pageable pageable) {


        // 서비스 호출 시 pageable 전달
        Page<Recommendation> recommendationPage = subscribeService.getRecommendationsByEmail(email, pageable);

        // Page<Recommendation> 를 Page<RecommendationDto> 로 변환 (내용만 변환)
        Page<RecommendationDto> recommendationDtoPage = recommendationPage.map(RecommendationDto::new);

        model.addAttribute("email", email);
        // 모델에 DTO 리스트 대신 Page 객체 추가
        model.addAttribute("recommendationPage", recommendationDtoPage);
        // 기존 recommendations 속성은 제거하거나 비워둠 (템플릿 호환성 위해)
        // model.addAttribute("recommendations", recommendationDtoPage.getContent()); // 이렇게 하면 JS에서 문제 발생

        return "dashboard";
    }
}