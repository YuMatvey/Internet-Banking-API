package com.bankapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bankapi.repository.BankAccountRepository;
import com.bankapi.repository.TransactionRepository;
import com.bankapi.model.BankAccount;
import com.bankapi.model.Transaction;
import com.bankapi.dto.TransferRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private Long userId1;
    private Long userId2;
    private Long userId3;

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @BeforeEach
    void setUp() {
        transactionRepository.deleteAllInBatch();
        bankAccountRepository.deleteAllInBatch();

        BankAccount account1 = new BankAccount(new BigDecimal("1000.00"));
        BankAccount account2 = new BankAccount(new BigDecimal("500.00"));
        BankAccount account3 = new BankAccount(new BigDecimal("0.00"));

        account1 = bankAccountRepository.save(account1);
        account2 = bankAccountRepository.save(account2);
        account3 = bankAccountRepository.save(account3);

        this.userId1 = account1.getUserId();
        this.userId2 = account2.getUserId();
        this.userId3 = account3.getUserId();
    }


    @Test
    void testGetBalance_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/balance/{userId}", userId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId1))
                .andExpect(jsonPath("$.balance").value("1000.0"));
    }

    @Test
    void testGetBalance_NotFound() throws Exception {
        Long nonExistentUserId = 999L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/balance/{userId}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void testDeposit_Success() throws Exception {
        BigDecimal amount = new BigDecimal("200.00");
        BigDecimal initialBalance = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal expectedNewBalance = initialBalance.add(amount);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/deposit")
                        .param("userId", userId1.toString())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("successful"));

        BankAccount updatedAccount = bankAccountRepository.findById(userId1).get();
        assertEquals(expectedNewBalance, updatedAccount.getBalance(), "Баланс в БД должен быть увеличен после пополнения.");

        List<Transaction> transactions = transactionRepository.findByUserId(userId1);
        assertEquals(1, transactions.size(), "Должна быть создана одна запись транзакции.");
        Transaction depositTx = transactions.get(0);
        assertEquals("DEPOSIT", depositTx.getType());
        assertEquals(amount, depositTx.getAmount());
        assertEquals(expectedNewBalance, depositTx.getBalanceAfter());
        assertNull(depositTx.getRelatedUserId());
        assertNotNull(depositTx.getTimestamp());
    }

    @Test
    void testDeposit_UserNotFound() throws Exception {
        Long nonExistentUserId = 999L;
        BigDecimal amount = new BigDecimal("200.00");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/deposit")
                        .param("userId", nonExistentUserId.toString())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        List<Transaction> transactions = transactionRepository.findByUserId(nonExistentUserId);
        assertTrue(transactions.isEmpty(), "Запись транзакции НЕ должна быть создана, если пользователь не найден.");
    }

    @Test
    void testWithdraw_Success() throws Exception {
        BigDecimal amount = new BigDecimal("150.00");
        BigDecimal initialBalance = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal expectedNewBalance = initialBalance.subtract(amount);
        assertTrue(initialBalance.compareTo(amount) >= 0, "Начальный баланс должен быть достаточен для теста.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/withdraw")
                        .param("userId", userId1.toString())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("successful"));

        BankAccount updatedAccount = bankAccountRepository.findById(userId1).get();
        assertEquals(expectedNewBalance, updatedAccount.getBalance(), "Баланс в БД должен быть корректно уменьшен после снятия.");

        List<Transaction> transactions = transactionRepository.findByUserId(userId1);
        assertEquals(1, transactions.size(), "Должна быть создана одна запись транзакции.");
        Transaction withdrawTx = transactions.get(0);
        assertEquals("WITHDRAW", withdrawTx.getType());
        assertEquals(amount, withdrawTx.getAmount());
        assertEquals(expectedNewBalance, withdrawTx.getBalanceAfter());
        assertNull(withdrawTx.getRelatedUserId());
        assertNotNull(withdrawTx.getTimestamp());
    }

    @Test
    void testWithdraw_InsufficientFunds() throws Exception {
        BigDecimal amount = new BigDecimal("1000.00");
        Long userId = userId2;
        BigDecimal initialBalance = bankAccountRepository.findById(userId).get().getBalance();
        assertTrue(initialBalance.compareTo(amount) < 0, "Начальный баланс должен быть недостаточен для этого теста.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/withdraw")
                        .param("userId", userId.toString())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        BankAccount accountAfter = bankAccountRepository.findById(userId).get();
        assertEquals(initialBalance, accountAfter.getBalance(), "Баланс в БД НЕ должен измениться при недостаточности средств.");

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        assertTrue(transactions.isEmpty(), "Запись транзакции НЕ должна быть создана.");
    }

    @Test
    void testWithdraw_UserNotFound() throws Exception {
        Long nonExistentUserId = 999L;
        BigDecimal amount = new BigDecimal("100.00");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/withdraw")
                        .param("userId", nonExistentUserId.toString())
                        .param("amount", amount.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        List<Transaction> transactions = transactionRepository.findByUserId(nonExistentUserId);
        assertTrue(transactions.isEmpty(), "Запись транзакции НЕ должна быть создана, если пользователь не найден.");
    }

    @Test
    void testTransfer_Success() throws Exception {
        Long senderId = userId1;
        Long receiverId = userId2;
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal senderInitialBalance = bankAccountRepository.findById(senderId).get().getBalance();
        BigDecimal receiverInitialBalance = bankAccountRepository.findById(receiverId).get().getBalance();
        BigDecimal senderExpectedNewBalance = senderInitialBalance.subtract(amount);
        BigDecimal receiverExpectedNewBalance = receiverInitialBalance.add(amount);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderId(senderId);
        transferRequest.setReceiverId(receiverId);
        transferRequest.setAmount(amount);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))

                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("successful"));

        BankAccount updatedSenderAccount = bankAccountRepository.findById(senderId).get();
        BankAccount updatedReceiverAccount = bankAccountRepository.findById(receiverId).get();

        assertEquals(senderExpectedNewBalance, updatedSenderAccount.getBalance(), "Баланс отправителя должен быть уменьшен.");
        assertEquals(receiverExpectedNewBalance, updatedReceiverAccount.getBalance(), "Баланс получателя должен быть увеличен.");

        List<Transaction> senderTransactions = transactionRepository.findByUserId(senderId);
        List<Transaction> receiverTransactions = transactionRepository.findByUserId(receiverId);

        assertEquals(1, senderTransactions.size(), "Должна быть создана одна запись транзакции для отправителя.");
        assertEquals(1, receiverTransactions.size(), "Должна быть создана одна запись транзакции для получателя.");

        Transaction senderTx = senderTransactions.get(0);
        Transaction receiverTx = receiverTransactions.get(0);

        assertEquals(senderId, senderTx.getUserId());
        assertEquals(amount, senderTx.getAmount());
        assertEquals("TRANSFER_OUT", senderTx.getType());
        assertEquals(updatedSenderAccount.getBalance(), senderTx.getBalanceAfter());
        assertEquals(receiverId, senderTx.getRelatedUserId());
        assertNotNull(senderTx.getTimestamp());

        assertEquals(receiverId, receiverTx.getUserId());
        assertEquals(amount, receiverTx.getAmount());
        assertEquals("TRANSFER_IN", receiverTx.getType());
        assertEquals(updatedReceiverAccount.getBalance(), receiverTx.getBalanceAfter());
        assertEquals(senderId, receiverTx.getRelatedUserId());
        assertNotNull(receiverTx.getTimestamp());
    }

    @Test
    void testTransfer_SenderNotFound() throws Exception {
        Long nonExistentSenderId = 999L;
        Long receiverId = userId2;
        BigDecimal amount = new BigDecimal("100.00");

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderId(nonExistentSenderId);
        transferRequest.setReceiverId(receiverId);
        transferRequest.setAmount(amount);

        BigDecimal initialBalance1 = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal initialBalance2 = bankAccountRepository.findById(userId2).get().getBalance();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))

                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(initialBalance1, bankAccountRepository.findById(userId1).get().getBalance(), "Баланс userId1 не должен измениться при ошибке.");
        assertEquals(initialBalance2, bankAccountRepository.findById(userId2).get().getBalance(), "Баланс userId2 не должен измениться при ошибке.");

        assertTrue(transactionRepository.findByUserId(userId1).isEmpty(), "Не должно быть новых транзакций для userId1 при ошибке.");
        assertTrue(transactionRepository.findByUserId(userId2).isEmpty(), "Не должно быть новых транзакций для userId2 при ошибке.");
        assertTrue(transactionRepository.findByUserId(nonExistentSenderId).isEmpty(), "Не должно быть новых транзакций для несуществующего отправителя при ошибке.");
    }

    @Test
    void testTransfer_ReceiverNotFound() throws Exception {
        Long senderId = userId1;
        Long nonExistentReceiverId = 999L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderId(senderId);
        transferRequest.setReceiverId(nonExistentReceiverId);
        transferRequest.setAmount(amount);

        BigDecimal initialBalance1 = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal initialBalance2 = bankAccountRepository.findById(userId2).get().getBalance();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))

                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(initialBalance1, bankAccountRepository.findById(userId1).get().getBalance(), "Баланс userId1 не должен измениться при ошибке.");
        assertEquals(initialBalance2, bankAccountRepository.findById(userId2).get().getBalance(), "Баланс userId2 не должен измениться при ошибке.");

        assertTrue(transactionRepository.findByUserId(userId1).isEmpty(), "Не должно быть новых транзакций для userId1 при ошибке.");
        assertTrue(transactionRepository.findByUserId(userId2).isEmpty(), "Не должно быть новых транзакций для userId2 при ошибке.");
        assertTrue(transactionRepository.findByUserId(nonExistentReceiverId).isEmpty(), "Не должно быть новых транзакций для несуществующего получателя при ошибке.");
    }

    @Test
    void testTransfer_InsufficientFunds() throws Exception {
        Long senderId = userId2;
        Long receiverId = userId1;
        BigDecimal amount = new BigDecimal("1000.00");

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSenderId(senderId);
        transferRequest.setReceiverId(receiverId);
        transferRequest.setAmount(amount);

        BigDecimal initialBalance1 = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal initialBalance2 = bankAccountRepository.findById(userId2).get().getBalance();

        assertTrue(initialBalance2.compareTo(amount) < 0, "Баланс отправителя должен быть недостаточен для этого теста.");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequest)))

                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        assertEquals(initialBalance1, bankAccountRepository.findById(userId1).get().getBalance(), "Баланс userId1 не должен измениться при ошибке.");
        assertEquals(initialBalance2, bankAccountRepository.findById(userId2).get().getBalance(), "Баланс userId2 не должен измениться при ошибке.");

        assertTrue(transactionRepository.findByUserId(userId1).isEmpty(), "Не должно быть новых транзакций для userId1 при ошибке.");
        assertTrue(transactionRepository.findByUserId(userId2).isEmpty(), "Не должно быть новых транзакций для userId2 при ошибке.");
    }

    @Test
    void testTransfer_InvalidAmount() throws Exception {
        Long senderId = userId1;
        Long receiverId = userId2;

        TransferRequest transferRequestZero = new TransferRequest();
        transferRequestZero.setSenderId(senderId);
        transferRequestZero.setReceiverId(receiverId);
        transferRequestZero.setAmount(BigDecimal.ZERO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequestZero)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        TransferRequest transferRequestNegative = new TransferRequest();
        transferRequestNegative.setSenderId(senderId);
        transferRequestNegative.setReceiverId(receiverId);
        transferRequestNegative.setAmount(new BigDecimal("-100.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequestNegative)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        TransferRequest transferRequestToSelf = new TransferRequest();
        transferRequestToSelf.setSenderId(senderId);
        transferRequestToSelf.setReceiverId(senderId);
        transferRequestToSelf.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(transferRequestToSelf)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").exists());

        BigDecimal initialBalance1 = bankAccountRepository.findById(userId1).get().getBalance();
        BigDecimal initialBalance2 = bankAccountRepository.findById(userId2).get().getBalance();
        assertEquals(initialBalance1, bankAccountRepository.findById(userId1).get().getBalance(), "Баланс userId1 не должен измениться при ошибке.");
        assertEquals(initialBalance2, bankAccountRepository.findById(userId2).get().getBalance(), "Баланс userId2 не должен измениться при ошибке.");

        assertTrue(transactionRepository.findByUserId(userId1).isEmpty(), "Не должно быть новых транзакций для userId1 при ошибке.");
        assertTrue(transactionRepository.findByUserId(userId2).isEmpty(), "Не должно быть новых транзакций для userId2 при ошибке.");
    }

    @Test
    void testGetTransactions_All() throws Exception {
        Long userId = userId1;
        LocalDateTime now = LocalDateTime.now();
        Transaction tx1 = new Transaction(userId, new BigDecimal("100.00"), "DEPOSIT", now.minusDays(5), new BigDecimal("1100.00"), null);
        Transaction tx2 = new Transaction(userId, new BigDecimal("50.00"), "WITHDRAW", now.minusDays(2), new BigDecimal("1050.00"), null);
        Transaction tx3 = new Transaction(userId, new BigDecimal("20.00"), "DEPOSIT", now.minusDays(1), new BigDecimal("1070.00"), null);
        transactionRepository.saveAll(Arrays.asList(tx1, tx2, tx3));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/transactions/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testGetTransactions_WithDates() throws Exception {
        Long userId = userId1;
        LocalDateTime now = LocalDateTime.now();
        Transaction txEarly = new Transaction(userId, new BigDecimal("100.00"), "DEPOSIT", now.minusDays(10), new BigDecimal("1100.00"), null);
        Transaction txMiddle1 = new Transaction(userId, new BigDecimal("50.00"), "WITHDRAW", now.minusDays(5), new BigDecimal("1050.00"), null);
        Transaction txMiddle2 = new Transaction(userId, new BigDecimal("20.00"), "DEPOSIT", now.minusDays(3), new BigDecimal("1070.00"), null);
        Transaction txLate = new Transaction(userId, new BigDecimal("30.00"), "WITHDRAW", now.minusDays(1), new BigDecimal("1040.00"), null);
        transactionRepository.saveAll(Arrays.asList(txEarly, txMiddle1, txMiddle2, txLate));

        LocalDateTime startDate = now.minusDays(7);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/transactions/{userId}", userId)
                        .queryParam("startDate", startDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));

        LocalDateTime endDate = now.minusDays(4);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/transactions/{userId}", userId)
                        .queryParam("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));

        LocalDateTime rangeStartDate = now.minusDays(6);
        LocalDateTime rangeEndDate = now.minusDays(2);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/transactions/{userId}", userId)
                        .queryParam("startDate", rangeStartDate.toString())
                        .queryParam("endDate", rangeEndDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));

        LocalDateTime futureDate = now.plusDays(1);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/transactions/{userId}", userId)
                        .queryParam("startDate", futureDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}