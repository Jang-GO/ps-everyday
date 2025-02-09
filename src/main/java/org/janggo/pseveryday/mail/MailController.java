package org.janggo.pseveryday.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;


    @PostMapping("/mail/send/{email}")
    public String sendMail(@PathVariable String email) {
        mailService.sendMail(email);
        return "success";
    }
}
