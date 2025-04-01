package com.bankapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance", nullable = false)
    private Double balance;

    public BankAccount() {
    }

    public BankAccount(Double balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
