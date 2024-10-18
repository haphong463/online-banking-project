package com.windev.online_banking.controller;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.exception.InvalidTokenException;
import com.windev.online_banking.payload.request.LoginRequest;
import com.windev.online_banking.payload.request.PasswordResetRequest;
import com.windev.online_banking.payload.request.SignupRequest;
import com.windev.online_banking.payload.response.JwtResponse;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.security.JwtUtil;
import com.windev.online_banking.service.*;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailVerificationService emailVerificationService;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            String message = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        catch(BadCredentialsException | UsernameNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        userService.registerUser(signUpRequest);

        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email to verify your account."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully."));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String username, @RequestParam String otp) {
        boolean isValid = otpService.validateOtp(otp);
        if (isValid && otp.equals(otpService.getOtp(username))) {
            otpService.removeOtp(username);
            String jwt = jwtUtil.generateJwtToken(username);
            return ResponseEntity.ok(new JwtResponse(jwt, "Authentication successful."));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid OTP."));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Password reset link has been sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @Valid @RequestBody PasswordResetRequest resetRequest) {
        boolean isReset = passwordResetService.resetPassword(token, resetRequest.getNewPassword());
        if (isReset) {
            return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token."));
        }
    }

}
