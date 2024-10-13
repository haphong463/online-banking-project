package com.windev.online_banking.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
}
