# Internet Banking API

## 📌 Описание

Этот проект представляет собой REST API для управления банковскими счетами. API позволяет пользователям выполнять основные банковские операции.

## 🛠 Технологии
- **Java 17**
- **Spring Boot** (Spring Web, Spring Data JPA)
- **PostgreSQL**
- **Hibernate**
- **REST API**

## 📂 Структура базы данных

### Таблица пользователей (`bank_account`)
| user_id | balance |
|---------|---------|
| 1       | 1000.00 |
| 2       | 500.00  |
| 3       | 0.00    |

### Таблица истории операций (`bank_transaction`)
| id             | user_id | amount | type     | timestamp           | balance_after |
|----------------|---------|--------|----------|---------------------|---------------|
| 1              | 1       | 200.00 | DEPOSIT  | 2024-04-01 12:30:00 | 1200.00       |
| 2              | 2       | 50.00  | WITHDRAW | 2024-04-01 13:00:00 | 450.00        |
| 3              | 1       | 100.00 | DEPOSIT  | 2024-04-02 09:00:00 | 1100.00       |

Для развертывания базы данных выполните:
```bash
psql -U postgres -d bank_db -f database_dump.sql
````

## 🌐 API Методы

### 1️⃣ Получение баланса

**Запрос:**

```
GET /api/bank/balance/{userId}
```

**Пример использования:**

```bash
curl -X GET http://localhost:8080/api/bank/balance/1
```

или

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/balance/1" -Method Get
```

**Ответ:**

```json
{
  "userId": 1,
  "balance": 1000.00
}
```

(*Примечание: поле "balance" теперь может быть представлено с большей точностью благодаря использованию BigDecimal.*)

### 2️⃣ Пополнение счета

**Запрос:**

```
POST /api/bank/deposit?userId={userId}&amount={amount}
```

**Пример использования:**

```bash
curl -X POST "http://localhost:8080/api/bank/deposit?userId=1&amount=200.00"
```

или

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/deposit?userId=1&amount=200.00" -Method Post

```

**Ответ:**

```json
{
  "status": 1,
  "message": "Баланс успешно пополнен"
}
```

### 3️⃣ Снятие средств

**Запрос:**

```
POST /api/bank/withdraw?userId={userId}&amount={amount}
```

**Пример использования:**

```bash
curl -X POST "http://localhost:8080/api/bank/withdraw?userId=2&amount=50.00"
```

или

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/withdraw?userId=1&amount=50.00" -Method Post
```

**Ответ:**

```json
{
  "status": 1,
  "message": "Операция успешна"
}
```

### 4️⃣ Получение истории операций

**Описание:** Позволяет получить список операций для конкретного пользователя. Можно опционально фильтровать операции по диапазону дат и времени, используя параметры `startDate` и `endDate`.

**Запрос:**

```
GET /api/bank/transactions/{userId}
```

**Параметры запроса (Query Parameters):**

  * `startDate`: (Необязательно) Начало периода фильтрации в формате ISO 8601 (`YYYY-MM-DDTHH:mm:ss`, например, `2024-01-01T00:00:00`).
  * `endDate`: (Необязательно) Конец периода фильтрации в формате ISO 8601 (`YYYY-MM-DDTHH:mm:ss`, например, `2024-12-31T23:59:59`).

**Пример использования (получить все операции для пользователя 1):**

```bash
curl -X GET "http://localhost:8080/api/bank/transactions/1"
```

или

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/transactions/1" -Method Get
```

**Пример использования (получить операции для пользователя 1 в диапазоне дат):**

```bash
curl -X GET "http://localhost:8080/api/bank/transactions/1?startDate=2024-04-01T00:00:00&endDate=2024-04-30T23:59:59"
```

или

```bash
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/transactions/1?startDate=2024-04-01T00%3A00%3A00&endDate=2024-04-30T23%3A59%3A59" -Method Get
```

(В команде `Invoke-RestMethod` двоеточия в параметрах даты/времени нужно закодировать как `%3A`).

**Ответ:**
Возвращает массив объектов транзакций.

```json
[
  {
    "id": 1,
    "userId": 1,
    "amount": 200.00,
    "type": "DEPOSIT",
    "timestamp": "2024-04-01T12:30:00",
    "balanceAfter": 1200.00
  },
  {
    "id": 2,
    "userId": 1,
    "amount": 50.00,
    "type": "WITHDRAW",
    "timestamp": "2024-04-02T09:00:00",
    "balanceAfter": 1150.00
  }
  // ... другие операции пользователя 1 за указанный период
]
```

## 🚀 Развертывание

1.  Установите PostgreSQL и создайте базу данных
2.  Импортируйте дамп базы данных
3.  Запустите приложение Spring Boot:

<!-- end list -->

```bash
mvn spring-boot:run
```

## 📷 Скриншоты

Структура базы данных:
