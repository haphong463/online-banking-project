package com.windev.online_banking.service;


public interface EmailService {
    void sendOtpEmail(String to, String subject, String text);
}
