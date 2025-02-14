package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SubscriberController {

    // 화면 조회
    @GetMapping()
    public String Home() { return "home";}


}
