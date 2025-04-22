package com.bankapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
    public BankAccount() {
    }

    public BankAccount(BigDecimal balance) {
        this.balance = balance;
    }


    public BankAccount(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }


    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}