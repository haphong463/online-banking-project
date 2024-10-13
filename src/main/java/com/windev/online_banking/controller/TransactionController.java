package com.windev.online_banking.controller;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.Transaction;
import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.request.TransferRequest;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.service.AccountService;
import com.windev.online_banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@Valid @RequestBody TransferRequest transferRequest, Authentication authentication) {
        String username = authentication.getName();

        Optional<User> senderOpt = userRepository.findByUsername(username);
        if (senderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Sender not found."));
        }

        Optional<Account> senderAccountOpt = accountService.getAccountByUser(senderOpt.get());
        if (senderAccountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Sender account not found."));
        }

        Optional<Account> receiverAccountOpt = accountService.getAccountByAccountNumber(transferRequest.getToAccountNumber());
        if (receiverAccountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Receiver account not found."));
        }

        Account senderAccount = senderAccountOpt.get();
        Account receiverAccount = receiverAccountOpt.get();

        BigDecimal amount = transferRequest.getAmount();
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            return ResponseEntity.badRequest().body(new MessageResponse("Insufficient balance."));
        }

        try {
            // Cập nhật số dư
            accountService.updateBalance(senderAccount, amount.negate().doubleValue());
            accountService.updateBalance(receiverAccount, amount.doubleValue());

            // Tạo giao dịch
            Transaction transaction = transactionService.createTransaction(senderAccount, receiverAccount, amount);

            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("An error occurred during the transaction."));
        }
    }


    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(Authentication authentication) {
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
        return ResponseEntity.ok(transactionService.getTransactionsForAccount(account));
    }
}
