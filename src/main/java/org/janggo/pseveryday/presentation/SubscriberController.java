package org.janggo.pseveryday.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.application.subscriber.service.SubscribeService;
import org.janggo.pseveryday.domain.problem.dto.ProblemLevel;
import org.janggo.pseveryday.domain.problem.repository.TagRepository;
import org.janggo.pseveryday.domain.recommendation.dto.RecommendationDto;
import org.janggo.pseveryday.domain.recommendation.entity.Recommendation;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.util.exception.custom.SubscriberNotFoundException;
import org.janggo.pseveryday.util.message.FailureMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Optional;

import static org.janggo.pseveryday.util.message.FailureMessage.*;

@Controller
@RequiredArgsConstructor
@Slf4j
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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> savePreferences(@RequestParam("email") String email,
                                          @RequestParam(value = "minTier", required = false) Integer minTier,
                                          @RequestParam(value = "maxTier", required = false) Integer maxTier,
                                          @RequestParam(value = "tags", required = false) List<Long> tagIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            subscribeService.subscribe(email, minTier , maxTier , tagIds);
            response.put("success", true);
            response.put("message", "구독이 완료되었습니다! 매일 8시에 알고리즘 문제를 보내드립니다.");
            log.info("{} 님 구독 성공", email);
            return ResponseEntity.ok(response); // 성공 시 200 OK 와 함께 JSON 응답 반환
        } catch (Exception e) {
            // 실제 운영 환경에서는 로깅 추가 권장
            // log.error("구독 처리 중 오류 발생: email={}, error={}", email, e.getMessage());
            response.put("success", false);
            response.put("message", "구독 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
            log.warn("{} 님 구독 실패, 사유 : {}", email, e.getMessage());
            // 서버 내부 오류이므로 500 Internal Server Error 반환 고려
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
            throw new SubscriberNotFoundException(SUBSCRIBER_NOT_FOUND.format(email));
        }

        return "mail/unsubscribe-result";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam("email") String email, Model model,
                            @PageableDefault Pageable pageable) {

        // 구독자 정보 조회
        Optional<Subscriber> subscriberOpt = subscribeService.findByEmail(email);
        if (subscriberOpt.isEmpty()) {
            throw new SubscriberNotFoundException(SUBSCRIBER_NOT_FOUND.format(email));
        }

        // 구독자 정보를 모델에 추가
        Subscriber subscriber = subscriberOpt.get();
        model.addAttribute("subscriber", subscriber);

        // 서비스 호출 시 pageable 전달
        Page<Recommendation> recommendationPage = subscribeService.getRecommendationsByEmail(email, pageable);

        // Page<Recommendation> 를 Page<RecommendationDto> 로 변환 (내용만 변환)
        Page<RecommendationDto> recommendationDtoPage = recommendationPage.map(RecommendationDto::new);

        model.addAttribute("email", email);
        // 모델에 DTO 리스트 대신 Page 객체 추가
        model.addAttribute("recommendationPage", recommendationDtoPage);

        return "dashboard";
    }

    @GetMapping("/settings")
    public String editPreferences(@RequestParam("email") String email, Model model) {
        // 기존 구독 정보 조회
        Optional<Subscriber> subscriber = subscribeService.findByEmail(email);
        if (subscriber.isEmpty()) {
            throw new SubscriberNotFoundException("구독자를 찾을 수 없습니다: " + email);
        }

        model.addAttribute("email", email);
        model.addAttribute("selectedMinTier", subscriber.get().getTierPreference().getMinTier());
        model.addAttribute("selectedMaxTier", subscriber.get().getTierPreference().getMaxTier());
        model.addAttribute("selectedTags", subscriber.get().getTagPreferenceNames()); // tagId 리스트 반환하도록 구현
        model.addAttribute("problemLevels", ProblemLevel.values());
        model.addAttribute("availableTags", tagRepository.findAll());
        return "settings"; // 선호정보 수정용 뷰
    }


}