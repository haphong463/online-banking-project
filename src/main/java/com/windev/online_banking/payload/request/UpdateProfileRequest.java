package com.windev.online_banking.payload.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest {
    @NotBlank
    @Email
    private String email;

    // Thêm các trường khác nếu cần, ví dụ: phone, address, v.v.
}
