package com.windev.online_banking.service.impl;

import com.windev.online_banking.service.OtpService;
import com.windev.online_banking.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private SmsService smsService;

    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    @Override
    public String generateOtp() {
        // Tạo OTP 6 chữ số
        int otp = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    @Override
    public void storeOtp(String username, String otp) {
        otpStorage.put(username, otp);
    }

    @Override
    public boolean validateOtp(String otp) {
        return otpStorage.containsValue(otp);
    }

    @Override
    public String getOtp(String username) {
        return otpStorage.get(username);
    }

    @Override
    public void removeOtp(String username) {
        otpStorage.remove(username);
    }
}
