package org.janggo.pseveryday.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 기본 실행 스레드 수
        executor.setMaxPoolSize(20);  // 최대 스레드 수
        executor.setQueueCapacity(500);  // 작업 대기 큐 크기
        executor.setThreadNamePrefix("MailAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);  // 종료 시 최대 1분간 대기
        executor.initialize();
        return executor;
    }
}