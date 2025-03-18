package org.janggo.pseveryday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PsEverydayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsEverydayApplication.class, args);
    }

}
