package com.windev.online_banking.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email
    @Column(unique = true)
    private String email;

    private String role; // e.g., USER, ADMIN

    private Boolean enabled; // Để quản lý trạng thái tài khoản

    private String otp; // OTP cho MFA

    private Long otpExpiration; // Thời gian hết hạn OTP

    private String resetToken;

    private String phoneNumber; // Thêm trường số điện thoại

    private Date resetTokenExpiration;

    private int failedLoginAttempts;

    private boolean accountNonLocked;

    private boolean emailVerified;

    private String verificationToken;
}