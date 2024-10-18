// src/test/java/com/windev/online_banking/service/impl/UserServiceImplTest.java
package com.windev.online_banking.service.impl;

import com.windev.online_banking.entity.User;
import com.windev.online_banking.exception.BankingException;
import com.windev.online_banking.payload.request.SignupRequest;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.security.JwtUtil;
import com.windev.online_banking.service.EmailService;
import com.windev.online_banking.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    private SignupRequest signupRequest;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
        user.setEnabled(true);
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);


        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("encodedPassword");
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User registeredUser = userService.registerUser(signupRequest);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals("USER", registeredUser.getRole());
        assertTrue(registeredUser.getEnabled());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterUser_UsernameExists() {
        // Arrange
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.ofNullable(user));

        // Act & Assert
        BankingException exception = assertThrows(BankingException.class, () -> {
            userService.registerUser(signupRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        assertEquals(400, exception.getStatus().value());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailExists() {
        // Arrange
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.ofNullable(user));

        // Act & Assert
        BankingException exception = assertThrows(BankingException.class, () -> {
            userService.registerUser(signupRequest);
        });

        assertEquals("Email already in use", exception.getMessage());
        assertEquals(400, exception.getStatus().value());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        String password = "encodedPassword";
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // Create a mock Authentication object
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
        when(mockAuthentication.getName()).thenReturn(user.getUsername());

        when(jwtUtil.generateJwtToken(user.getUsername())).thenReturn("jwtToken");
        when(otpService.generateOtp()).thenReturn("123456");

        // Act
        String response = userService.loginUser(user.getUsername(), password);

        // Assert
        assertEquals("OTP sent to your email", response);
        assertEquals(0, user.getFailedLoginAttempts());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).save(user);
        verify(jwtUtil, times(1)).generateJwtToken(user.getUsername());
        verify(otpService, times(1)).generateOtp();
        verify(otpService, times(1)).storeOtp(user.getUsername(), "123456");
        verify(emailService, times(1)).sendOtpEmail(user.getEmail(), "Your OTP Code", "Your OTP code is: 123456");
    }
    @Test
    void testLoginUser_InvalidCredentials() {
        // Arrange
        String wrongPassword = "wrongPassword";
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // Act & Assert
        org.springframework.security.authentication.BadCredentialsException exception = assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> userService.loginUser(user.getUsername(), wrongPassword)
        );

        assertEquals("Invalid username or password.", exception.getMessage());
        assertEquals(1, user.getFailedLoginAttempts());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).save(user);
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testLoginUser_AccountLockedAfterMaxFailedAttempts() {
        // Arrange
        String wrongPassword = "wrongPassword";
        user.setFailedLoginAttempts(4);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // Act & Assert
        org.springframework.security.authentication.BadCredentialsException exception = assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> userService.loginUser(user.getUsername(), wrongPassword)
        );

        assertEquals("Invalid username or password.", exception.getMessage());
        assertEquals(5, user.getFailedLoginAttempts());
        assertFalse(user.isAccountNonLocked());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).save(user);
        verify(emailService, times(1)).sendOtpEmail(
                eq(user.getEmail()),
                eq("Account Locked"),
                eq("Your account has been locked due to multiple failed login attempts.")
        );
    }

    @Test
    void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        org.springframework.security.core.userdetails.UsernameNotFoundException exception = assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> userService.loginUser(user.getUsername(), "anyPassword")
        );

        assertEquals("Username not found: " + user.getUsername(), exception.getMessage());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(authenticationManager, never()).authenticate(any());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), anyString());
    }

    // Thêm các bài kiểm tra cho verifyOtp nếu cần
}
