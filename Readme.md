Internet Banking API

📌 Описание

Этот проект представляет собой REST API для управления банковскими счетами. API позволяет пользователям выполнять следующие операции:

🔍 Получение баланса по user_id

💰 Пополнение счета (deposit)

💸 Снятие средств (withdraw)

API возвращает ответы в формате JSON.

🛠 Технологии

Java 17

Spring Boot (Spring Web, Spring Data JPA)

PostgreSQL

Hibernate

REST API

📂 Структура базы данных

База данных состоит из двух таблиц:

Таблица пользователей (bank_account)

user_id

balance

1

1000.00

2

500.00

3

0.00

Таблица истории операций (transaction_history)

transaction_id

user_id

type

amount

timestamp

related_user_id

1

1

deposit

200.00

2024-04-01 12:30:00

NULL

2

2

withdraw

50.00

2024-04-01 13:00:00

NULL

3

1

transfer

100.00

2024-04-01 14:15:00

2

📷 Скриншот структуры базы данных: screenshots/db_structure.png
Для развертывания базы данных из дампа выполните:

psql -U postgres -d bank_db -f database_dump.sql

🌐 API Методы

1️⃣ Получение баланса

Запрос:

GET /api/bank/balance/{userId}

Пример использования:

curl -X GET http://localhost:8080/api/bank/balance/1

Ответ:

{
  "userId": 1,
  "balance": 1000.00
}

2️⃣ Пополнение счета

Запрос:

POST /api/bank/deposit?userId={userId}&amount={amount}

Пример использования:

curl -X POST "http://localhost:8080/api/bank/deposit?userId=1&amount=200.00"

Ответ:

{
  "status": 1,
  "message": "Баланс успешно пополнен"
}

3️⃣ Снятие средств

Запрос:

POST /api/bank/withdraw?userId={userId}&amount={amount}

Пример использования:

curl -X POST "http://localhost:8080/api/bank/withdraw?userId=2&amount=50.00"

Ответ:

{
  "status": 1,
  "message": "Операция успешна"
}

4️⃣ Перевод средств

Запрос:

POST /api/bank/transfer?fromUserId={fromUserId}&toUserId={toUserId}&amount={amount}

Пример использования:

curl -X POST "http://localhost:8080/api/bank/transfer?fromUserId=1&toUserId=2&amount=100.00"

Ответ:

{
  "status": 1,
  "message": "Перевод выполнен успешно"
}
