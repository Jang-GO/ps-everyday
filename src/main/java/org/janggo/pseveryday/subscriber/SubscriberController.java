package org.janggo.pseveryday.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SubscriberController {

    // 화면 조회
    @GetMapping()
    public String Home() { return "home";}
}
