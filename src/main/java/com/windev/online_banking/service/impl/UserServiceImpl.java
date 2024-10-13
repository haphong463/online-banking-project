// src/main/java/com/example/banking/service/impl/UserServiceImpl.java
package com.windev.online_banking.service.impl;


import com.windev.online_banking.entity.User;
import com.windev.online_banking.exception.BankingException;
import com.windev.online_banking.payload.response.JwtResponse;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.security.JwtUtil;
import com.windev.online_banking.service.EmailService;
import com.windev.online_banking.service.OtpService;
import com.windev.online_banking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    private Random random = new Random();

    @Override
    public User registerUser(User user) {
        if(userRepository.findByUsername(user.getUsername()) != null){
            throw new BankingException("Username already exists", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByEmail(user.getEmail()) != null){
            throw new BankingException("Email already in use", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public String loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);

        if(user != null){
            if (!user.isAccountNonLocked()) {
                throw new RuntimeException("Your account has been locked due to multiple failed login attempts. Please contact support.");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password));

                user.setFailedLoginAttempts(0);
                userRepository.save(user);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtil.generateJwtToken(authentication.getName());

                String otp = otpService.generateOtp();
                otpService.storeOtp(username, otp);
                String email = user.getEmail();
                emailService.sendOtpEmail(email, "Your OTP Code", "Your OTP code is: " + otp);

                return "OTP sent to your email";
            } catch (BadCredentialsException e) {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountNonLocked(false);
                    emailService.sendOtpEmail(user.getEmail(), "Account Locked", "Your account has been locked due to multiple failed login attempts.");
                }
                userRepository.save(user);
                throw new BadCredentialsException("Invalid username or password.");
            }
        }else{
            throw new UsernameNotFoundException("Username not found: " + username);
        }


    }

    @Override
    public void verifyOtp(String username, String otp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        if(user == null){
            throw new BankingException("User not found", HttpStatus.NOT_FOUND);
        }

        if(user.getOtp() == null || user.getOtpExpiration() < System.currentTimeMillis()){
            throw new BankingException("OTP expired or not found", HttpStatus.BAD_REQUEST);
        }

        if(!user.getOtp().equals(otp)){
            throw new BankingException("Invalid OTP", HttpStatus.BAD_REQUEST);
        }

        // Reset OTP
        user.setOtp(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
    }

    private void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP for Online Banking Login");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 5 minutes.");
        mailSender.send(message);
    }
}
