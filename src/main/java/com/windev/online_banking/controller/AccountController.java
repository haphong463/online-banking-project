package com.windev.online_banking.controller;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.User;
import com.windev.online_banking.payload.response.AccountResponse;
import com.windev.online_banking.payload.response.MessageResponse;
import com.windev.online_banking.repository.UserRepository;
import com.windev.online_banking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserAccount(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        Optional<Account> accountOpt = accountService.getAccountByUser(userOpt.get());
        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Account not found.");
        }

        Account account = accountOpt.get();
        AccountResponse response = new AccountResponse(account.getAccountNumber(), account.getBalance(), account.getAccountType());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestParam String accountType, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
        }

        User user = userOpt.get();

        // Kiểm tra xem người dùng đã có tài khoản với loại này chưa
        Optional<Account> existingAccount = accountService.getAccountByUserAndType(user, accountType.toUpperCase());
        if (existingAccount.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Account of type " + accountType + " already exists."));
        }

        // Tạo tài khoản mới
        Account newAccount = accountService.createAccount(user, accountType);
        AccountResponse response = new AccountResponse(newAccount.getAccountNumber(), newAccount.getBalance(), newAccount.getAccountType());
        return ResponseEntity.ok(response);
    }


    // Thêm các endpoint khác nếu cần (ví dụ: tạo tài khoản mới)
}
