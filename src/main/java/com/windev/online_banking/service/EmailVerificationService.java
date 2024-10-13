package com.windev.online_banking.service;

import com.windev.online_banking.entity.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user);
    boolean verifyEmail(String token);
}
