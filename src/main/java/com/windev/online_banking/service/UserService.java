package com.windev.online_banking.service;

import com.windev.online_banking.entity.User;

public interface UserService {
    User registerUser(User user);
    String loginUser(String username, String password);
    void verifyOtp(String username, String otp);
}
