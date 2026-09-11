package com.threedolphins.broadcast.model;

public class SendResult {

    private final boolean success;
    private final String message;

    private SendResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static SendResult success() {
        return new SendResult(true, "Message sent successfully");
    }

    public static SendResult failure(String message) {
        return new SendResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}