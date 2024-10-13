package com.windev.online_banking.service;

public interface OtpService {
    String generateOtp();
    boolean validateOtp(String otp);
    void storeOtp(String username, String otp);
    String getOtp(String username);
    void removeOtp(String username);
}
