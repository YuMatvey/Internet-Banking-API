package com.bankapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "related_user_id", nullable = true)
    private Long relatedUserId;


    public Transaction() {
    }

    public Transaction(Long userId, BigDecimal amount, String type, LocalDateTime timestamp, BigDecimal balanceAfter) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
    }

    public Transaction(Long userId, BigDecimal amount, String type, LocalDateTime timestamp, BigDecimal balanceAfter, Long relatedUserId) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
        this.relatedUserId = relatedUserId;
    }



    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    public Long getRelatedUserId() {
        return relatedUserId;
    }

    public void setRelatedUserId(Long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }
}