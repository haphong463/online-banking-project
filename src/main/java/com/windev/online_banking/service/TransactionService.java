package com.windev.online_banking.service;

import com.windev.online_banking.entity.Transaction;
import com.windev.online_banking.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction createTransaction(Account fromAccount, Account toAccount, BigDecimal amount);
    List<Transaction> getTransactionsForAccount(Account account);
}
