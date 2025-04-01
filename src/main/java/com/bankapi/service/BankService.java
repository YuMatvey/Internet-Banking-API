package com.bankapi.service;

import com.bankapi.model.BankAccount;
import com.bankapi.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BankService {

    @Autowired
    private BankAccountRepository accountRepository;
    public Double getBalance(Long userId) {
        Optional<BankAccount> account = accountRepository.findById(userId);
        return account.map(BankAccount::getBalance).orElse(-1.0);
    }
    @Transactional
    public int putMoney(Long userId, Double amount) {
        Optional<BankAccount> accountOpt = accountRepository.findById(userId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
            return 1;
        }
        return 0;
    }
    @Transactional
    public int takeMoney(Long userId, Double amount) {
        Optional<BankAccount> accountOpt = accountRepository.findById(userId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                accountRepository.save(account);
                return 1;
            }
            return 0; // Недостаточно средств
        }
        return 0; // Пользователь не найден или другая ошибка
    }
}
