package com.windev.online_banking.payload.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class PasswordResetRequest {
    @NotBlank
    private String email; // Chỉ dùng cho yêu cầu quên mật khẩu

    @NotBlank
    @Size(min = 6, max = 40)
    private String newPassword; // Chỉ dùng cho yêu cầu đặt lại mật khẩu
}
