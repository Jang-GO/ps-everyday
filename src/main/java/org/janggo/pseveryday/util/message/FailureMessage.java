package org.janggo.pseveryday.util.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FailureMessage {

    SUBSCRIBER_NOT_FOUND("%s님의 구독정보가 존재하지 않습니다.");

    private final String template;

    public String format(Object... args) {
        return String.format(template, args);
    }
}
