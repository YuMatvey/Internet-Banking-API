package com.bankapi.repository;

import com.bankapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByUserIdAndTimestampAfter(Long userId, LocalDateTime startDate);

    List<Transaction> findByUserIdAndTimestampBefore(Long userId, LocalDateTime endDate);
}