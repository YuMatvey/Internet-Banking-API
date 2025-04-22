package com.bankapi.controller;

import com.bankapi.model.Transaction;
import com.bankapi.service.BankService;
import com.bankapi.dto.TransferRequest;
import com.bankapi.exception.UserNotFoundException;
import com.bankapi.exception.InsufficientFundsException;
import com.bankapi.exception.InvalidAmountException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// import java.util.Collections;
import java.util.HashMap; // Импорт
import java.util.List;
import java.util.Map; // Импорт
// import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        BigDecimal balance = bankService.getBalance(userId);

        if (balance == null) {
            response.put("status", 0);
            response.put("message", "Not found user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("userId", userId);
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> putMoney(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        int result = bankService.putMoney(userId, amount);

        if (result == 1) {
            response.put("status", 1);
            response.put("message", "successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 0);
            response.put("message", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> takeMoney(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        int result = bankService.takeMoney(userId, amount);

        if (result == 1) {
            response.put("status", 1);
            response.put("message", "successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 0);
            response.put("message", "fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/transactions/{userId}")
    public List<Transaction> getTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return bankService.getOperationList(userId, startDate, endDate);
    }


    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {

        bankService.transferMoney(request.getSenderId(), request.getReceiverId(), request.getAmount());

        Map<String, Object> response = new HashMap<>();
        response.put("status", 1);
        response.put("message", "successful");

        return ResponseEntity.ok(response);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFundsException(InsufficientFundsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAmountException(InvalidAmountException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}