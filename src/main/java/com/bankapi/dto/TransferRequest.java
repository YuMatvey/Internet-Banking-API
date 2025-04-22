package com.bankapi.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Long senderId; // ID отправителя
    private Long receiverId; // ID получателя
    private BigDecimal amount; // Сумма перевода

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return Long.valueOf(receiverId); // Возвращаем Long
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}