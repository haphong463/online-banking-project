package com.windev.online_banking.service;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.User;

import java.util.Optional;

public interface AccountService {
    Optional<Account> getAccountByUser(User user);
    Optional<Account> getAccountByAccountNumber(String accountNumber);
    Account createAccount(User user, String accountType);
    void updateBalance(Account account, double amount);

    Optional<Account> getAccountByUserAndType(User user, String upperCase);
}
