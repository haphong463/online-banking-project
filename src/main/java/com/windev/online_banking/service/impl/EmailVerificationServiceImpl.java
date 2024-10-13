package com.windev.online_banking.service.impl;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.exception.InvalidTokenException;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.service.EmailService;
import com.windev.online_banking.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + token;
        emailService.sendOtpEmail(user.getEmail(), "Email Verification", "Click the link to verify your email: " + verificationLink);
    }

    @Override
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(user -> token.equals(user.getVerificationToken()) && user.isEmailVerified() == false)
                .findFirst();

        if (userOpt.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired verification token.");
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return true;
    }
}
