package com.windev.online_banking.repository;

import com.windev.online_banking.entity.Account;
import com.windev.online_banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUser(User user);
    Optional<Account> findByUserAndAccountType(User user, String accountType);
}
