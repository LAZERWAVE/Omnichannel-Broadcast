package com.threedolphins.broadcast.config;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class ExecutorConfig {

    private final ExecutorService executorService =Executors.newFixedThreadPool(5);

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}