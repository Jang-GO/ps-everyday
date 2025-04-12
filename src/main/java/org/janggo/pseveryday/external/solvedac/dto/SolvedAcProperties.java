package org.janggo.pseveryday.external.solvedac.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "boj.page")
public class SolvedAcProperties {
    private int start;
    private int end;
}
