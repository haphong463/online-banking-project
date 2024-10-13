package com.windev.online_banking.controller;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.Transaction;
import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.payload.response.ReportResponse;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.service.AccountService;
import com.windev.online_banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Endpoint để lấy báo cáo giao dịch trong khoảng thời gian.
     * URL: GET /api/reports/transactions
     * Params: startDate, endDate (ví dụ: 2023-01-01, 2023-12-31)
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionReport(@RequestParam String startDate,
                                                  @RequestParam String endDate,
                                                  Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
        }

        Optional<Account> accountOpt = accountService.getAccountByUser(userOpt.get());
        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Account not found."));
        }

        Account account = accountOpt.get();
        List<Transaction> transactions = transactionService.getTransactionsForAccount(account);

        // Lọc giao dịch theo khoảng thời gian
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Transaction> filteredTransactions = transactions.stream()
                .filter(t -> !t.getTimestamp().toLocalDate().isBefore(start) && !t.getTimestamp().toLocalDate().isAfter(end))
                .toList();

        // Tính tổng số tiền giao dịch
        BigDecimal totalAmount = filteredTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReportResponse report = new ReportResponse(filteredTransactions, totalAmount);
        return ResponseEntity.ok(report);
    }
}
