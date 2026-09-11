package com.threedolphins.broadcast.config;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class ExecutorConfig {

    private static final int WORKER_THREAD_COUNT = 5;

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(WORKER_THREAD_COUNT);

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}