package com.windev.online_banking.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank
    private String toAccountNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero.")
    private BigDecimal amount;
}
