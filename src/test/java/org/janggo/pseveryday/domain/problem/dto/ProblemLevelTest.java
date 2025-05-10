package org.janggo.pseveryday.domain.problem.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ProblemLevelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("레벨이 주어질 때 ProblemLevel enum 으로 역직렬화가 정상적으로 수행되는지 테스트")
    void fromLevel() throws JsonProcessingException {
        // given
        String json = "15";

        // when
        ProblemLevel problemLevel = objectMapper.readValue(json, ProblemLevel.class);

        // then
        assertThat(problemLevel).isEqualTo(ProblemLevel.GOLD_I);
    }



}