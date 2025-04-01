# Internet Banking API

## 📌 Описание
Этот проект реализует REST API для работы с банковскими счетами. API поддерживает следующие функции:

- 📌 Получение баланса по `user_id`
- 💰 Пополнение счета (`deposit`)
- 💸 Снятие средств (`withdraw`)

## 🛠 Технологии
- Java 17
- Spring Boot
- PostgreSQL
- Hibernate
- REST API

## 📂 Структура базы данных
Наша база данных состоит из таблицы пользователей:

| user_id | balance  |
|---------|---------|
| 1       | 1000.00 |
| 2       | 500.00  |
| 3       | 0.00    |

📷 **Скриншот структуры базы данных:**
![DB Structure](screenshots/db_structure.png)

## 📄 Импорт базы данных
Чтобы развернуть базу данных локально, выполните:

```bash
psql -U postgres -d bank_db -f database_dump.sql
#   I n t e r n e t - B a n k i n g - A P I  
 