package com.windev.online_banking.service.impl;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.exception.UserNotFoundException;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.service.EmailService;
import com.windev.online_banking.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User with email " + email + " not found.");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))); // 1 giờ

        userRepository.save(user);

        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        emailService.sendOtpEmail(email, "Password Reset Request", "Click the link to reset your password: " + resetLink);
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(user -> token.equals(user.getResetToken()) && user.getResetTokenExpiration().after(new Date()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);

        return true;
    }
}
