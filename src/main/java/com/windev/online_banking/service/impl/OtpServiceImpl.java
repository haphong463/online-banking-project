package com.windev.online_banking.service.impl;

import com.windev.online_banking.service.OtpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OtpServiceImpl implements OtpService {

    private Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private SecureRandom random = new SecureRandom();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public boolean validateOtp(String otp) {
        return otpStorage.containsValue(otp);
    }

    @Override
    public void storeOtp(String username, String otp) {
        otpStorage.put(username, otp);
        // Xoá OTP sau 5 phút
        scheduler.schedule(() -> otpStorage.remove(username), 5, TimeUnit.MINUTES);
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
