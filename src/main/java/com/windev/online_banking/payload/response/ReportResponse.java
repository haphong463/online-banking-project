package com.windev.online_banking.payload.response;

import com.windev.online_banking.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ReportResponse {
    private List<Transaction> transactions;
    private BigDecimal totalAmount;
}
