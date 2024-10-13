package com.windev.online_banking.service.impl;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.User;
import com.windev.online_banking.repository.AccountRepository;
import com.windev.online_banking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Optional<Account> getAccountByUser(User user) {
        return accountRepository.findByUser(user);
    }

    @Override
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Account createAccount(User user, String accountType) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType.toUpperCase());
        return accountRepository.save(account);
    }

    @Override
    public void updateBalance(Account account, double amount) {
        account.setBalance(account.getBalance().add(BigDecimal.valueOf(amount)));
        accountRepository.save(account);
    }

    @Override
    public Optional<Account> getAccountByUserAndType(User user, String upperCase) {
        return accountRepository.findByUserAndAccountType(user, upperCase);
    }

    private String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
