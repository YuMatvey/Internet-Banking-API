package com.bankapi.service;

import com.bankapi.exception.InsufficientFundsException;
import com.bankapi.exception.InvalidAmountException;
import com.bankapi.exception.UserNotFoundException;
import com.bankapi.model.BankAccount;
import com.bankapi.model.Transaction;
import com.bankapi.repository.BankAccountRepository;
import com.bankapi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BankService {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal getBalance(Long userId) {
        Optional<BankAccount> account = accountRepository.findById(userId);
        return account.map(BankAccount::getBalance).orElse(null);
    }

    @Transactional
    public int putMoney(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            return 0;
        }

        Optional<BankAccount> accountOpt = accountRepository.findById(userId);

        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);


            System.out.println("--- Service Debug: Account state before saving (putMoney) ---");
            System.out.println("Account userId: " + account.getUserId());
            System.out.println("Account balance: " + account.getBalance());
            System.out.println("------------------------------------------------------------");


            accountRepository.save(account);

            Transaction transaction = new Transaction(userId, amount, "DEPOSIT", LocalDateTime.now(), newBalance, null);
            transactionRepository.save(transaction);

            return 1;
        } else {

            return 0;
        }
    }


    @Transactional
    public int takeMoney(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        Optional<BankAccount> accountOpt = accountRepository.findById(userId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            if (account.getBalance().compareTo(amount) >= 0) {
                BigDecimal newBalance = account.getBalance().subtract(amount);
                account.setBalance(newBalance);

                System.out.println("--- Service Debug: Account state before saving (takeMoney) ---");
                System.out.println("Account userId: " + account.getUserId());
                System.out.println("Account balance: " + account.getBalance());
                System.out.println("-------------------------------------------------------------");

                accountRepository.save(account);
                Transaction transaction = new Transaction(userId, amount, "WITHDRAW", LocalDateTime.now(), newBalance, null);
                transactionRepository.save(transaction);

                return 1;
            }
            return 0;
        } else {
            return 0;
        }
    }

    @Transactional
    public void transferMoney(Long senderId, Long receiverId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive.");
        }

        if (senderId.equals(receiverId)) {
            throw new InvalidAmountException("Cannot transfer money to yourself.");
        }


        Optional<BankAccount> senderAccountOpt = accountRepository.findById(senderId);
        if (!senderAccountOpt.isPresent()) {
            throw new UserNotFoundException("Sender user not found with ID: " + senderId);
        }
        BankAccount senderAccount = senderAccountOpt.get();
        Optional<BankAccount> receiverAccountOpt = accountRepository.findById(receiverId);
        if (!receiverAccountOpt.isPresent()) {
            throw new UserNotFoundException("Receiver user not found with ID: " + receiverId);
        }
        BankAccount receiverAccount = receiverAccountOpt.get();
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for user ID: " + senderId);
        }
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

        System.out.println("--- Service Debug: Account state before saving (transferMoney - Sender) ---");
        System.out.println("Account userId: " + senderAccount.getUserId());
        System.out.println("Account balance: " + senderAccount.getBalance());
        System.out.println("-------------------------------------------------------------------------");

        accountRepository.save(senderAccount);
        System.out.println("--- Service Debug: Account state before saving (transferMoney - Receiver) ---");
        System.out.println("Account userId: " + receiverAccount.getUserId());
        System.out.println("Account balance: " + receiverAccount.getBalance());
        System.out.println("--------------------------------------------------------------------------");

        accountRepository.save(receiverAccount);
        Transaction senderTx = new Transaction(senderId, amount, "TRANSFER_OUT", LocalDateTime.now(), senderAccount.getBalance(), receiverId);
        Transaction receiverTx = new Transaction(receiverId, amount, "TRANSFER_IN", LocalDateTime.now(), receiverAccount.getBalance(), senderId);

        transactionRepository.save(senderTx);
        transactionRepository.save(receiverTx);
    }

    public List<Transaction> getOperationList(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return transactionRepository.findByUserIdAndTimestampBetween(userId, startDate, endDate);
        } else if (startDate != null) {
            return transactionRepository.findByUserIdAndTimestampAfter(userId, startDate);
        } else if (endDate != null) {
            return transactionRepository.findByUserIdAndTimestampBefore(userId, endDate);
        } else {
            return transactionRepository.findByUserId(userId);
        }
    }

}