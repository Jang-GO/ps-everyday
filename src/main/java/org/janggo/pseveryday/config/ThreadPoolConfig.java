package org.janggo.pseveryday.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    @Bean("subscriberProcessingExecutor") // 빈 이름 지정
    public ExecutorService subscriberProcessingExecutor() {
        // CPU 코어 수 기반으로 스레드 풀 크기 결정 (I/O 바운드 작업을 고려하여 조금 더 크게 설정 가능)
        int coreCount = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(4, coreCount * 2); // 예시: 최소 4개, 코어 수의 2배

        // 고정된 크기의 스레드 풀 사용 또는 다른 ExecutorService 구현체 선택 가능
        // 여기서는 newFixedThreadPool을 사용. 필요에 따라 ThreadPoolTaskExecutor 등으로 커스터마이징 가능
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize,
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setName("SubscriberProcess-" + thread.getId()); // 스레드 이름 설정
                    return thread;
                });

        log.info("SubscriberProcessingExecutor 생성 완료. Pool size: {}", poolSize);
        return executorService;
    }
}
