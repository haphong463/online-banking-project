package com.windev.online_banking.service;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.request.SignupRequest;

public interface UserService {
    User registerUser(SignupRequest signupRequest);
    String loginUser(String username, String password);
    void verifyOtp(String username, String otp);
}
