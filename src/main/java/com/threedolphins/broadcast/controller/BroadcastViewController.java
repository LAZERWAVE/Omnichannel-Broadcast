package com.threedolphins.broadcast.controller;

import org.primefaces.PrimeFaces;

import com.threedolphins.broadcast.model.BroadcastJob;
import com.threedolphins.broadcast.model.BroadcastStatus;
import com.threedolphins.broadcast.model.Customer;
import com.threedolphins.broadcast.model.CustomerStatus;
import com.threedolphins.broadcast.service.BroadcastService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;


import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Named("broadcastView")
@ViewScoped
public class BroadcastViewController implements Serializable {

    private static final int MAX_MESSAGE_LENGTH = 500;

    private String message;

    private List<Customer> customers;

    private BroadcastJob currentJob;

    private BroadcastStatus status = BroadcastStatus.IDLE;

    @Inject
    private BroadcastService broadcastService;

    @PostConstruct
    public void init() {

        customers = new ArrayList<>();

        customers.add(new Customer(1L, "John Doe", "081234567801"));
        customers.add(new Customer(2L, "Jane Smith", "081234567802"));
        customers.add(new Customer(3L, "Michael Brown", "081234567803"));
        customers.add(new Customer(4L, "Sarah Wilson", "081234567804"));
        customers.add(new Customer(5L, "David Miller", "081234567805"));
        customers.add(new Customer(6L, "Emily Davis", "081234567806"));
        customers.add(new Customer(7L, "Daniel Garcia", "081234567807"));
        customers.add(new Customer(8L, "Olivia Martinez", "081234567808"));
        customers.add(new Customer(9L, "James Anderson", "081234567809"));
        customers.add(new Customer(10L, "Sophia Taylor", "081234567810"));
    }

    public void startBroadcast() {

        if (message == null || message.isBlank()) {

            FacesContext.getCurrentInstance().addMessage(
                    "broadcastForm:message",
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Message required",
                            "Please enter a message before starting the broadcast."
                    )
            );

            return;
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {

            FacesContext.getCurrentInstance().addMessage(
                    "broadcastForm:message",
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Message too long",
                            "Message must not exceed "
                                    + MAX_MESSAGE_LENGTH
                                    + " characters."
                    )
            );

            return;
        }

        if (status == BroadcastStatus.RUNNING) {
            return;
        }

        currentJob =
                broadcastService.startBroadcast(
                        customers,
                        message
                );

        status = BroadcastStatus.RUNNING;
    }

    public String getCustomerStatusClass(Long customerId) {

        if (currentJob == null) {
            return "status-pending";
        }

        CustomerStatus customerStatus =
                currentJob.getStatus(customerId);

        if (customerStatus == null) {
            return "status-pending";
        }

        switch (customerStatus) {

            case SENT:
                return "status-sent";

            case FAILED:
                return "status-failed";

            case PROCESSING:
                return "status-processing";

            case PENDING:
            default:
                return "status-pending";
        }
    }

    public String getCustomerStatusLabel(Long customerId) {

        if (currentJob == null) {
            return "PENDING";
        }

        CustomerStatus customerStatus =
                currentJob.getStatus(customerId);

        return customerStatus == null
                ? "PENDING"
                : customerStatus.name();
    }

    public String getStartTime() {

        if (currentJob == null) {
            return "-";
        }

        return formatTime(currentJob.getStartTimeMillis());
    }

    public String getEndTime() {

        if (currentJob == null ||
                currentJob.getEndTimeMillis() == null) {

            return "-";
        }

        return formatTime(currentJob.getEndTimeMillis());
    }

    public String getElapsedTime() {

        if (currentJob == null) {
            return "-";
        }

        long endTime =
                currentJob.getEndTimeMillis() != null
                        ? currentJob.getEndTimeMillis()
                        : System.currentTimeMillis();

        long elapsedSeconds =
                Duration.ofMillis(
                        endTime - currentJob.getStartTimeMillis()
                ).getSeconds();

        return formatDuration(elapsedSeconds);
    }

    public String getEstimatedTimeRemaining() {

        if (currentJob == null ||
                currentJob.isCompleted()) {

            return "-";
        }

        int processed = currentJob.getProcessedCount();

        if (processed == 0) {
            return "Calculating...";
        }

        long elapsedMillis =
                System.currentTimeMillis()
                        - currentJob.getStartTimeMillis();

        double averageMillisPerCustomer =
                (double) elapsedMillis / processed;

        int remaining =
                currentJob.getTotalCount() - processed;

        long estimatedMillis =
                (long) (averageMillisPerCustomer * remaining);

        long estimatedSeconds =
                Math.max(
                        1,
                        Duration.ofMillis(estimatedMillis).getSeconds()
                );

        return "~" + formatDuration(estimatedSeconds);
    }

    private String formatTime(long millis) {

        return java.time.LocalDateTime
                .ofInstant(
                        Instant.ofEpochMilli(millis),
                        java.time.ZoneId.systemDefault()
                )
                .format(
                        java.time.format.DateTimeFormatter
                                .ofPattern("HH:mm:ss")
                );
    }

    private String formatDuration(long seconds) {

        if (seconds < 60) {
            return seconds + " seconds";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        return minutes + "m "
                + remainingSeconds + "s";
    }

    public void checkProgress() {
        if (currentJob != null && currentJob.isCompleted()) {

            currentJob.markCompleted();
            status = BroadcastStatus.COMPLETED;

            PrimeFaces.current()
                    .executeScript("PF('broadcastPoll').stop()");
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public BroadcastJob getCurrentJob() {
        return currentJob;
    }

    public BroadcastStatus getStatus() {
        return status;
    }

    public boolean isBroadcastRunning() {
        return status == BroadcastStatus.RUNNING;
    }

    public boolean isBroadcastCompleted() {
        return currentJob != null && currentJob.isCompleted();
    }

    public int getProgress() {
        return currentJob == null ? 0 : currentJob.getProgress();
    }

    public int getSentCount() {
        return currentJob == null ? 0 : currentJob.getSentCount();
    }

    public int getFailedCount() {
        return currentJob == null ? 0 : currentJob.getFailedCount();
    }

    public int getProcessedCount() {
        return currentJob == null ? 0 : currentJob.getProcessedCount();
    }

    public int getTotalCount() {
        return customers == null ? 0 : customers.size();
    }
}