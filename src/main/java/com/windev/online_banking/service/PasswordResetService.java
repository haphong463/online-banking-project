package com.windev.online_banking.service;

public interface PasswordResetService {
    void initiatePasswordReset(String email);
    boolean resetPassword(String token, String newPassword);
}
