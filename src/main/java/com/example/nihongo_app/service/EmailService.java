package com.example.nihongo_app.service;

public interface EmailService {

    void sendRankReminder(String recipient, String displayName, int daysRemaining, int decayExp);
}