package com.windev.online_banking.service.impl;

import com.windev.online_banking.entity.Transaction;
import com.windev.online_banking.entity.Account;
import com.windev.online_banking.repository.TransactionRepository;
import com.windev.online_banking.service.AccountService;
import com.windev.online_banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final BigDecimal MAX_TRANSACTION_AMOUNT = BigDecimal.valueOf(10000.00); // Ví dụ: 10,000
    private final BigDecimal DAILY_TRANSACTION_LIMIT = BigDecimal.valueOf(20000.00); // Ví dụ: 20,000

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Override
    public Transaction createTransaction(Account fromAccount, Account toAccount, BigDecimal amount) {
        // Kiểm tra giới hạn giao dịch
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new IllegalArgumentException("Transaction amount exceeds the maximum limit.");
        }

        // Kiểm tra tổng số tiền giao dịch trong ngày
        BigDecimal totalToday = transactionRepository.findByFromAccountIdOrToAccountId(fromAccount.getId(), fromAccount.getId())
                .stream()
                .filter(t -> t.getTimestamp().toLocalDate().equals(LocalDate.now()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalToday.add(amount).compareTo(DAILY_TRANSACTION_LIMIT) > 0) {
            throw new IllegalArgumentException("Daily transaction limit exceeded.");
        }

        // Phát hiện gian lận (ví dụ: nhiều giao dịch từ nhiều địa điểm)
        // Bạn có thể tích hợp thêm các logic phức tạp hơn ở đây

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("COMPLETED"); // Hoặc "PENDING" nếu cần xử lý thêm

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsForAccount(Account account) {
        return transactionRepository.findByFromAccountIdOrToAccountId(account.getId(), account.getId());
    }
}
