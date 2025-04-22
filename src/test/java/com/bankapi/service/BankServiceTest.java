package com.bankapi.service;

import com.bankapi.model.BankAccount;
import com.bankapi.model.Transaction;
import com.bankapi.repository.BankAccountRepository;
import com.bankapi.repository.TransactionRepository;
import com.bankapi.exception.UserNotFoundException;
import com.bankapi.exception.InsufficientFundsException;
import com.bankapi.exception.InvalidAmountException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class BankServiceTest {

    @Mock
    private BankAccountRepository mockAccountRepository;
    @Mock
    private TransactionRepository mockTransactionRepository;

    @InjectMocks
    private BankService bankService;
    private Transaction createTransaction(Long userId, BigDecimal amount, String type, LocalDateTime timestamp, BigDecimal balanceAfter, Long relatedUserId) {
        Transaction tx = new Transaction(userId, amount, type, timestamp, balanceAfter, relatedUserId);
        return tx;
    }
    static class BankAccountMatcher implements ArgumentMatcher<BankAccount> {
        private Long expectedUserId;
        private BigDecimal expectedBalance;

        public BankAccountMatcher(Long expectedUserId, BigDecimal expectedBalance) {
            this.expectedUserId = expectedUserId;
            this.expectedBalance = expectedBalance;
        }

        @Override
        public boolean matches(BankAccount argument) {
            System.out.println("--- BankAccountMatcher: Проверка аргумента ---");
            System.out.println("Полученный Account userId: " + (argument != null ? argument.getUserId() : "null аргумент"));
            System.out.println("Полученный Account balance: " + (argument != null ? argument.getBalance() : "null аргумент"));
            System.out.println("Ожидаемый Account userId: " + expectedUserId);
            System.out.println("Ожидаемый Account balance: " + expectedBalance);
            System.out.println("---------------------------------------------");


            if (argument == null) {
                return false;
            }
            boolean userIdMatches = expectedUserId == null ? argument.getUserId() == null : expectedUserId.equals(argument.getUserId());

            boolean balanceMatches = expectedBalance == null ? argument.getBalance() == null : expectedBalance.compareTo(argument.getBalance()) == 0;

            return userIdMatches && balanceMatches;
        }

        @Override
        public String toString() {
            return String.format("BankAccount with userId=%s and balance=%s", expectedUserId, expectedBalance);
        }
    }

    @Test
    void testGetBalance_UserFound() {
        Long userId = 1L;
        BigDecimal expectedBalance = new BigDecimal("1000.50");
        BankAccount account = new BankAccount(expectedBalance);
        account.setUserId(userId);

        when(mockAccountRepository.findById(userId)).thenReturn(Optional.of(account));
        BigDecimal actualBalance = bankService.getBalance(userId);
        assertEquals(expectedBalance, actualBalance, "Баланс должен совпадать");
        verify(mockAccountRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testGetBalance_UserNotFound() {
        Long userId = 99L;
        when(mockAccountRepository.findById(userId)).thenReturn(Optional.empty());
        BigDecimal actualBalance = bankService.getBalance(userId);
        assertNull(actualBalance, "Для несуществующего пользователя должен вернуться null");
        verify(mockAccountRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }
    @Test
    void testPutMoney_Success() {
        Long userId = 1L;
        BigDecimal initialBalance = new BigDecimal("500.00");
        BigDecimal amount = new BigDecimal("200.00");
        BigDecimal expectedNewBalance = initialBalance.add(amount);

        BankAccount accountBeforeOperation = new BankAccount(initialBalance);
        accountBeforeOperation.setUserId(userId);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(mockAccountRepository.findById(userId)).thenReturn(Optional.of(accountBeforeOperation));
        when(mockAccountRepository.save(any(BankAccount.class))).thenReturn(null);

        when(mockTransactionRepository.save(transactionCaptor.capture())).thenReturn(null);
        int result = bankService.putMoney(userId, amount);
        assertEquals(1, result, "putMoney должен вернуть 1 при успехе.");
        verify(mockAccountRepository, times(1)).findById(userId);

        verify(mockAccountRepository, times(1)).save(argThat(new BankAccountMatcher(userId, expectedNewBalance)));
        verify(mockTransactionRepository, times(1)).save(any(Transaction.class));
        Transaction savedTx = transactionCaptor.getValue();
        assertEquals(userId, savedTx.getUserId(), "UserID в транзакции должен совпадать.");
        assertEquals(amount, savedTx.getAmount(), "Сумма в транзакции должна совпадать с суммой пополнения.");
        assertEquals("DEPOSIT", savedTx.getType(), "Тип транзакции должен быть DEPOSIT.");
        assertEquals(expectedNewBalance, savedTx.getBalanceAfter(), "BalanceAfter в транзакции должен быть равен новому балансу.");
        assertNotNull(savedTx.getTimestamp(), "Timestamp в транзакции должен быть установлен.");
        assertNull(savedTx.getRelatedUserId(), "RelatedUserId в транзакции DEPOSIT должен быть null.");
        verifyNoMoreInteractions(mockAccountRepository, mockTransactionRepository);
    }
    @Test
    void testPutMoney_UserNotFound() {
        Long userId = 99L;
        BigDecimal amount = new BigDecimal("200.00");
        when(mockAccountRepository.findById(userId)).thenReturn(Optional.empty());
        int result = bankService.putMoney(userId, amount);
        assertEquals(0, result, "putMoney должен вернуть 0, если пользователь не найден.");
        verify(mockAccountRepository, times(1)).findById(userId);
        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }
    @Test
    void testTakeMoney_Success() {
        Long userId = 2L;
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal amount = new BigDecimal("150.00");
        BigDecimal expectedNewBalance = initialBalance.subtract(amount);

        BankAccount accountBeforeOperation = new BankAccount(initialBalance);
        accountBeforeOperation.setUserId(userId);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(mockAccountRepository.findById(userId)).thenReturn(Optional.of(accountBeforeOperation));
        when(mockAccountRepository.save(any(BankAccount.class))).thenReturn(null);

        when(mockTransactionRepository.save(transactionCaptor.capture())).thenReturn(null);
        int result = bankService.takeMoney(userId, amount);
        assertEquals(1, result, "takeMoney должен вернуть 1 при успехе.");
        verify(mockAccountRepository, times(1)).findById(userId);

        verify(mockAccountRepository, times(1)).save(argThat(new BankAccountMatcher(userId, expectedNewBalance)));
        verify(mockTransactionRepository, times(1)).save(any(Transaction.class));
        Transaction savedTx = transactionCaptor.getValue();
        assertEquals(userId, savedTx.getUserId(), "UserID в транзакции должен совпадать.");
        assertEquals(amount, savedTx.getAmount(), "Сумма в транзакции должна совпадать с суммой снятия.");
        assertEquals("WITHDRAW", savedTx.getType(), "Тип транзакции должен быть WITHDRAW.");
        assertEquals(expectedNewBalance, savedTx.getBalanceAfter(), "BalanceAfter в транзакции должен быть равен новому балансу.");
        assertNotNull(savedTx.getTimestamp(), "Timestamp в транзакции должен быть установлен.");
        assertNull(savedTx.getRelatedUserId(), "RelatedUserId в транзакции WITHDRAW должен быть null.");

        verifyNoMoreInteractions(mockAccountRepository, mockTransactionRepository);
    }

    @Test
    void testTakeMoney_InsufficientFunds() {
        Long userId = 3L;
        BigDecimal initialBalance = new BigDecimal("50.00");
        BigDecimal amount = new BigDecimal("100.00");
        BankAccount account = new BankAccount(initialBalance);
        account.setUserId(userId);

        when(mockAccountRepository.findById(userId)).thenReturn(Optional.of(account));
        int result = bankService.takeMoney(userId, amount);
        assertEquals(0, result, "takeMoney должен вернуть 0 при недостаточности средств.");
        verify(mockAccountRepository, times(1)).findById(userId);
        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testTakeMoney_UserNotFound() {
        Long userId = 99L;
        BigDecimal amount = new BigDecimal("100.00");
        when(mockAccountRepository.findById(userId)).thenReturn(Optional.empty());
        int result = bankService.takeMoney(userId, amount);
        assertEquals(0, result, "takeMoney должен вернуть 0, если пользователь не найден.");
        verify(mockAccountRepository, times(1)).findById(userId);
        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testTransferMoney_Success() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal senderInitialBalance = new BigDecimal("500.00");
        BigDecimal receiverInitialBalance = new BigDecimal("200.00");
        BigDecimal senderExpectedNewBalance = senderInitialBalance.subtract(amount);
        BigDecimal receiverExpectedNewBalance = receiverInitialBalance.add(amount);

        BankAccount senderAccountBeforeOperation = new BankAccount(senderInitialBalance);
        senderAccountBeforeOperation.setUserId(senderId);

        BankAccount receiverAccountBeforeOperation = new BankAccount(receiverInitialBalance);
        receiverAccountBeforeOperation.setUserId(receiverId);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(mockAccountRepository.findById(senderId)).thenReturn(Optional.of(senderAccountBeforeOperation));
        when(mockAccountRepository.findById(receiverId)).thenReturn(Optional.of(receiverAccountBeforeOperation));
        when(mockAccountRepository.save(any(BankAccount.class))).thenReturn(null);

        when(mockTransactionRepository.save(transactionCaptor.capture())).thenReturn(null);
        assertDoesNotThrow(() -> bankService.transferMoney(senderId, receiverId, amount),
                "Метод transferMoney не должен бросать исключение при успешном переводе.");
        verify(mockAccountRepository, times(1)).findById(senderId);
        verify(mockAccountRepository, times(1)).findById(receiverId);
        verify(mockAccountRepository, times(1)).save(argThat(new BankAccountMatcher(senderId, senderExpectedNewBalance)));
        verify(mockAccountRepository, times(1)).save(argThat(new BankAccountMatcher(receiverId, receiverExpectedNewBalance)));
        verify(mockAccountRepository, times(2)).save(any(BankAccount.class));
        verify(mockTransactionRepository, times(2)).save(any(Transaction.class));

        List<Transaction> capturedTransactions = transactionCaptor.getAllValues();
        assertEquals(2, capturedTransactions.size(), "Должно быть захвачено 2 объекта Transaction при сохранении.");
        Transaction capturedSenderTx = capturedTransactions.stream()
                .filter(tx -> senderId.equals(tx.getUserId()) && "TRANSFER_OUT".equals(tx.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция отправителя (TRANSFER_OUT) не найдена среди захваченных аргументов."));
        Transaction capturedReceiverTx = capturedTransactions.stream()
                .filter(tx -> receiverId.equals(tx.getUserId()) && "TRANSFER_IN".equals(tx.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция получателя (TRANSFER_IN) не найдена среди захваченных аргументов."));
        assertEquals(senderId, capturedSenderTx.getUserId(), "UserID в захваченной транзакции отправителя должен совпадать.");
        assertEquals(amount, capturedSenderTx.getAmount(), "Сумма в захваченной транзакции отправителя должна совпадать.");
        assertEquals("TRANSFER_OUT", capturedSenderTx.getType(), "Тип захваченной транзакции отправителя должен быть TRANSFER_OUT.");
        assertEquals(senderExpectedNewBalance, capturedSenderTx.getBalanceAfter(), "BalanceAfter в захваченной транзакции отправителя должен быть равен новому балансу отправителя.");
        assertEquals(receiverId, capturedSenderTx.getRelatedUserId(), "RelatedUserId в захваченной транзакции отправителя должен быть ID получателя.");
        assertNotNull(capturedSenderTx.getTimestamp(), "Timestamp в захваченной транзакции отправителя должен быть установлен.");
        assertEquals(receiverId, capturedReceiverTx.getUserId(), "UserID в захваченной транзакции получателя должен совпадать.");
        assertEquals(amount, capturedReceiverTx.getAmount(), "Сумма в захваченной транзакции получателя должна совпадать.");
        assertEquals("TRANSFER_IN", capturedReceiverTx.getType(), "Тип захваченной транзакции получателя должен быть TRANSFER_IN.");
        assertEquals(receiverExpectedNewBalance, capturedReceiverTx.getBalanceAfter(), "BalanceAfter в захваченной транзакции получателя должен быть равен новому балансу получателя.");
        assertEquals(senderId, capturedReceiverTx.getRelatedUserId(), "RelatedUserId в захваченной транзакции получателя должен быть ID отправителя.");
        assertNotNull(capturedReceiverTx.getTimestamp(), "Timestamp в захваченной транзакции получателя должен быть установлен.");
        verifyNoMoreInteractions(mockAccountRepository, mockTransactionRepository);
    }
    @Test
    void testTransferMoney_SenderNotFound() {
        Long senderId = 99L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        when(mockAccountRepository.findById(senderId)).thenReturn(Optional.empty());
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            bankService.transferMoney(senderId, receiverId, amount);
        }, "Должно быть брошено UserNotFoundException, если отправитель не найден.");
        assertTrue(thrown.getMessage().contains("Sender user not found"), "Сообщение исключения должно указывать на отправителя.");
        verify(mockAccountRepository, times(1)).findById(senderId);
        verify(mockAccountRepository, never()).findById(receiverId);

        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testTransferMoney_ReceiverNotFound() {
        Long senderId = 1L;
        Long receiverId = 99L;
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal senderInitialBalance = new BigDecimal("500.00");
        BankAccount senderAccount = new BankAccount(senderId,senderInitialBalance);

        when(mockAccountRepository.findById(senderId)).thenReturn(Optional.of(senderAccount));
        when(mockAccountRepository.findById(receiverId)).thenReturn(Optional.empty());
        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            bankService.transferMoney(senderId, receiverId, amount);
        }, "Должно быть брошено UserNotFoundException, если получатель не найден.");
        assertTrue(thrown.getMessage().contains("Receiver user not found"), "Сообщение исключения должно указывать на получателя.");
        verify(mockAccountRepository, times(1)).findById(senderId);
        verify(mockAccountRepository, times(1)).findById(receiverId);

        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }


    @Test
    void testTransferMoney_InsufficientFunds() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal senderInitialBalance = new BigDecimal("500.00");
        BigDecimal receiverInitialBalance = new BigDecimal("200.00");
        BankAccount senderAccount = new BankAccount(senderId, senderInitialBalance);
        BankAccount receiverAccount = new BankAccount(receiverId, receiverInitialBalance);

        when(mockAccountRepository.findById(senderId)).thenReturn(Optional.of(senderAccount));
        when(mockAccountRepository.findById(receiverId)).thenReturn(Optional.of(receiverAccount));
        InsufficientFundsException thrown = assertThrows(InsufficientFundsException.class, () -> {
            bankService.transferMoney(senderId, receiverId, amount);
        }, "Должно быть брошено InsufficientFundsException, если недостаточно средств.");
        assertTrue(thrown.getMessage().contains("Insufficient funds"), "Сообщение исключения должно указывать на недостаточность средств.");
        verify(mockAccountRepository, times(1)).findById(senderId);
        verify(mockAccountRepository, times(1)).findById(receiverId);
        verify(mockAccountRepository, never()).save(any(BankAccount.class));
        verify(mockTransactionRepository, never()).save(any(Transaction.class));
        verifyNoMoreInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testTransferMoney_InvalidAmount_ZeroOrNegative() {
        Long senderId = 1L;
        Long receiverId = 2L;

        InvalidAmountException thrownZero = assertThrows(InvalidAmountException.class, () -> {
            bankService.transferMoney(senderId, receiverId, BigDecimal.ZERO);
        }, "Должно быть брошено InvalidAmountException для нулевой суммы.");
        assertTrue(thrownZero.getMessage().contains("Transfer amount must be positive"), "Сообщение исключения должно указывать на положительную сумму.");
        InvalidAmountException thrownNegative = assertThrows(InvalidAmountException.class, () -> {
            bankService.transferMoney(senderId, receiverId, new BigDecimal("-100.00"));
        }, "Должно быть брошено InvalidAmountException для отрицательной суммы.");
        assertTrue(thrownNegative.getMessage().contains("Transfer amount must be positive"), "Сообщение исключения должно указывать на положительную сумму.");

        verifyNoInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }

    @Test
    void testTransferMoney_InvalidAmount_ToSelf() {
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        InvalidAmountException thrown = assertThrows(InvalidAmountException.class, () -> {
            bankService.transferMoney(userId, userId, amount);
        }, "Должно быть брошено InvalidAmountException при попытке перевести самому себе.");
        assertTrue(thrown.getMessage().contains("Cannot transfer money to yourself"), "Сообщение исключения должно указывать на перевод самому себе.");

        verifyNoInteractions(mockAccountRepository);
        verifyNoInteractions(mockTransactionRepository);
    }


    @Test
    void testGetOperationList_All() {
        Long userId = 1L;
        List<Transaction> expectedTransactions = Arrays.asList(
                createTransaction(userId, new BigDecimal("100.00"), "DEPOSIT", LocalDateTime.now().minusDays(2), new BigDecimal("600.00"), null),
                createTransaction(userId, new BigDecimal("50.00"), "WITHDRAW", LocalDateTime.now().minusDays(1), new BigDecimal("550.00"), null)
        );
        when(mockTransactionRepository.findByUserId(userId)).thenReturn(expectedTransactions);
        List<Transaction> actualTransactions = bankService.getOperationList(userId, null, null);
        assertEquals(expectedTransactions.size(), actualTransactions.size(), "Размер списка транзакций должен совпадать.");
        assertEquals(expectedTransactions, actualTransactions, "Списки транзакций должны совпадать.");
        verify(mockTransactionRepository, times(1)).findByUserId(userId);
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampAfter(anyLong(), any(LocalDateTime.class));
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBefore(anyLong(), any(LocalDateTime.class));
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));

        verifyNoMoreInteractions(mockTransactionRepository);
        verifyNoInteractions(mockAccountRepository);
    }

    @Test
    void testGetOperationList_FromStartDate() {
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);

        List<Transaction> expectedTransactions = Arrays.asList(
                createTransaction(userId, new BigDecimal("100.00"), "DEPOSIT", LocalDateTime.now().minusDays(2), new BigDecimal("600.00"), null),
                createTransaction(userId, new BigDecimal("50.00"), "WITHDRAW", LocalDateTime.now().minusDays(1), new BigDecimal("550.00"), null)
        );
        when(mockTransactionRepository.findByUserIdAndTimestampAfter(eq(userId), eq(startDate))).thenReturn(expectedTransactions);
        List<Transaction> actualTransactions = bankService.getOperationList(userId, startDate, null);
        assertEquals(expectedTransactions.size(), actualTransactions.size(), "Размер списка транзакций должен совпадать.");
        assertEquals(expectedTransactions, actualTransactions, "Списки транзакций должны совпадать.");

        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampAfter(eq(userId), eq(startDate));
        verify(mockTransactionRepository, never()).findByUserId(anyLong());
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBefore(anyLong(), any(LocalDateTime.class));
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));

        verifyNoMoreInteractions(mockTransactionRepository);
        verifyNoInteractions(mockAccountRepository);
    }

    @Test
    void testGetOperationList_BeforeEndDate() {
        Long userId = 1L;
        LocalDateTime endDate = LocalDateTime.now().minusDays(0);

        List<Transaction> expectedTransactions = Arrays.asList(
                createTransaction(userId, new BigDecimal("100.00"), "DEPOSIT", LocalDateTime.now().minusDays(2), new BigDecimal("600.00"), null),
                createTransaction(userId, new BigDecimal("50.00"), "WITHDRAW", LocalDateTime.now().minusDays(1), new BigDecimal("550.00"), null)
        );
        when(mockTransactionRepository.findByUserIdAndTimestampBefore(eq(userId), eq(endDate))).thenReturn(expectedTransactions);
        List<Transaction> actualTransactions = bankService.getOperationList(userId, null, endDate);
        assertEquals(expectedTransactions.size(), actualTransactions.size(), "Размер списка транзакций должен совпадать.");
        assertEquals(expectedTransactions, actualTransactions, "Списки транзакций должны совпадать.");


        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampBefore(eq(userId), eq(endDate));
        verify(mockTransactionRepository, never()).findByUserId(anyLong());
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampAfter(anyLong(), any(LocalDateTime.class));
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBetween(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));


        verifyNoMoreInteractions(mockTransactionRepository);
        verifyNoInteractions(mockAccountRepository);
    }

    @Test
    void testGetOperationList_BetweenDates() {
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        List<Transaction> expectedTransactions = Arrays.asList(
                createTransaction(userId, new BigDecimal("100.00"), "DEPOSIT", LocalDateTime.now().minusDays(2), new BigDecimal("600.00"), null)
        );
        when(mockTransactionRepository.findByUserIdAndTimestampBetween(eq(userId), eq(startDate), eq(endDate))).thenReturn(expectedTransactions);
        List<Transaction> actualTransactions = bankService.getOperationList(userId, startDate, endDate);
        assertEquals(expectedTransactions.size(), actualTransactions.size(), "Размер списка транзакций должен совпадать.");
        assertEquals(expectedTransactions, actualTransactions, "Списки транзакций должны совпадать.");


        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampBetween(eq(userId), eq(startDate), eq(endDate));
        verify(mockTransactionRepository, never()).findByUserId(anyLong());
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampAfter(anyLong(), any(LocalDateTime.class));
        verify(mockTransactionRepository, never()).findByUserIdAndTimestampBefore(anyLong(), any(LocalDateTime.class));


        verifyNoMoreInteractions(mockTransactionRepository);
        verifyNoInteractions(mockAccountRepository);
    }

    @Test
    void testGetOperationList_NoTransactionsFound() {
        Long userId = 5L;

        when(mockTransactionRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(mockTransactionRepository.findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(mockTransactionRepository.findByUserIdAndTimestampBefore(eq(userId), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(mockTransactionRepository.findByUserIdAndTimestampBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());


        List<Transaction> actualTransactionsAll = bankService.getOperationList(userId, null, null);
        assertTrue(actualTransactionsAll.isEmpty(), "Список транзакций должен быть пустым, если нет транзакций.");
        verify(mockTransactionRepository, times(1)).findByUserId(userId);
        List<Transaction> actualTransactionsStartDate = bankService.getOperationList(userId, LocalDateTime.now().minusDays(1), null);
        assertTrue(actualTransactionsStartDate.isEmpty(), "Список транзакций должен быть пустым, если нет транзакций.");
        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampAfter(eq(userId), any(LocalDateTime.class));
        List<Transaction> actualTransactionsEndDate = bankService.getOperationList(userId, null, LocalDateTime.now());
        assertTrue(actualTransactionsEndDate.isEmpty(), "Список транзакций должен быть пустым, если нет транзакций.");
        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampBefore(eq(userId), any(LocalDateTime.class));
        List<Transaction> actualTransactionsBetween = bankService.getOperationList(userId, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3));
        assertTrue(actualTransactionsBetween.isEmpty(), "Список транзакций должен быть пустым, если нет транзакций.");
        verify(mockTransactionRepository, times(1)).findByUserIdAndTimestampBetween(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
        verifyNoMoreInteractions(mockTransactionRepository);
        verifyNoInteractions(mockAccountRepository);
    }

}