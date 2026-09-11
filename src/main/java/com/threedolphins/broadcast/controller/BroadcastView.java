package com.threedolphins.broadcast.controller;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class BroadcastView implements Serializable {

    private String message;

    public void startBroadcast() {
        System.out.println("=================================");
        System.out.println("Priority Broadcast Started");
        System.out.println("Message: " + message);
        System.out.println("=================================");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}