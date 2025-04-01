package com.bankapi.controller;

import com.bankapi.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;
    @GetMapping("/balance/{userId}")
    public Map<String, Object> getBalance(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        Double balance = bankService.getBalance(userId);
        response.put("userId", userId);
        response.put("balance", balance);
        if (balance.equals(-1.0)) {
            response.put("error", "Пользователь не найден");
        }
        return response;
    }
    @PostMapping("/deposit")
    public Map<String, Object> putMoney(@RequestParam Long userId, @RequestParam Double amount) {
        Map<String, Object> response = new HashMap<>();
        int result = bankService.putMoney(userId, amount);
        response.put("userId", userId);
        response.put("result", result);
        if (result == 0) {
            response.put("error", "Ошибка при выполнении операции пополнения");
        }
        return response;
    }

    @PostMapping("/withdraw")
    public Map<String, Object> takeMoney(@RequestParam Long userId, @RequestParam Double amount) {
        Map<String, Object> response = new HashMap<>();
        int result = bankService.takeMoney(userId, amount);
        response.put("userId", userId);
        response.put("result", result);
        if (result == 0) {
            response.put("error", "Ошибка при выполнении операции снятия средств или недостаточно средств");
        }
        return response;
    }
}
