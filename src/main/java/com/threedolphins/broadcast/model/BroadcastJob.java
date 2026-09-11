package com.threedolphins.broadcast.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastJob {

    private final Map<Long, CustomerStatus> customerStatuses =
            new ConcurrentHashMap<>();

    private final AtomicInteger sentCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();

    public BroadcastJob(Iterable<Customer> customers) {
        for (Customer customer : customers) {
            customerStatuses.put(customer.getId(), CustomerStatus.PENDING);
        }
    }

    public Map<Long, CustomerStatus> getCustomerStatuses() {
        return customerStatuses;
    }

    public CustomerStatus getStatus(Long customerId) {
        return customerStatuses.get(customerId);
    }

    public void setStatus(Long customerId, CustomerStatus status) {
        customerStatuses.put(customerId, status);
    }

    public void incrementSent() {
        sentCount.incrementAndGet();
    }

    public void incrementFailed() {
        failedCount.incrementAndGet();
    }

    public int getSentCount() {
        return sentCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public int getProcessedCount() {
        return sentCount.get() + failedCount.get();
    }

    public int getTotalCount() {
        return customerStatuses.size();
    }

    public int getProgress() {
        if (getTotalCount() == 0) {
            return 0;
        }

        return (getProcessedCount() * 100) / getTotalCount();
    }

    public boolean isCompleted() {
        return getProcessedCount() >= getTotalCount();
    }
}