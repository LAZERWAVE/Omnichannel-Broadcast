package com.threedolphins.broadcast.service;

import com.threedolphins.broadcast.model.Customer;
import com.threedolphins.broadcast.model.SendResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class MockMessagingService {

    public SendResult send(Customer customer, String message) {

        try {
            // Simulate external API latency: 1–2 seconds
            long delay = ThreadLocalRandom.current().nextLong(1000, 2001);
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return SendResult.failure("Request interrupted");
        }

        // 10% simulated failure rate
        boolean failed = ThreadLocalRandom.current().nextInt(100) < 10;

        if (failed) {
            return SendResult.failure("Mock provider returned an error");
        }

        return SendResult.success();
    }
}