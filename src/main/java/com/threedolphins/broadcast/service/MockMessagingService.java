package com.threedolphins.broadcast.service;

import com.threedolphins.broadcast.model.Customer;
import com.threedolphins.broadcast.model.SendResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class MockMessagingService {

    private static final long MIN_DELAY_MILLIS = 1000;
    private static final long MAX_DELAY_MILLIS = 2000;

    private static final int FAILURE_RATE_PERCENT = 10;
    private static final int PERCENTAGE_RANGE = 100;

    public SendResult send(Customer customer, String message) {

        try {
            // Simulate external API latency: 1–2 seconds
            long delay = ThreadLocalRandom.current().nextLong(
                    MIN_DELAY_MILLIS,
                    MAX_DELAY_MILLIS + 1
            );

            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return SendResult.failure("Request interrupted");
        }

        // Simulate a 10% failure rate
        boolean failed =
                ThreadLocalRandom.current().nextInt(PERCENTAGE_RANGE)
                        < FAILURE_RATE_PERCENT;

        if (failed) {
            return SendResult.failure("Mock provider returned an error");
        }

        return SendResult.success();
    }
}