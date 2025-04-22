# Internet Banking API

## 📌 Описание

Этот проект представляет собой REST API для управления банковскими счетами. API позволяет пользователям выполнять основные банковские операции, такие как просмотр баланса, пополнение, снятие, перевод средств между счетами, а также просмотр истории транзакций.

## 🛠 Технологии
* **Java 17+** (рекомендуется JDK 21+)
* **Spring Boot** (Spring Web, Spring Data JPA)
* **PostgreSQL** (основная база данных)
* **H2 Database** (для интеграционных тестов)
* **Hibernate**
* **REST API**
* **Maven**
* **JUnit 5**
* **Mockito**

## ⚙️ Настройка и запуск проекта

### Предварительные требования

Убедитесь, что у вас установлены:

1.  Java Development Kit (JDK) версии 17 или выше.
2.  Система сборки Maven (https://maven.apache.org/download.cgi) или Gradle (https://gradle.org/install/).
3.  Запущенный экземпляр базы данных PostgreSQL.

### Сборка проекта

1.  Склонируйте репозиторий проекта:
    ```bash
    git clone <ссылка_на_ваш_репозиторий>
    cd <папка_проекта>
    ```
    (Замените `<ссылка_на_ваш_репозиторий>` на фактическую ссылку)

2.  Соберите проект с помощью Maven:
    ```bash
    mvn clean package -DskipTests
    ```
    Или с помощью Gradle:
    ```bash
    ./gradlew clean build -x test
    ```
    Флаг `-DskipTests` или `-x test` используется для пропуска выполнения тестов во время сборки. Если вы хотите запустить тесты, просто выполните `mvn clean package` или `./gradlew clean build`.

### Конфигурация базы данных

Приложение использует PostgreSQL в качестве основной базы данных. Параметры подключения настраиваются в файле `src/main/resources/application.properties`:

```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bankdb
spring.datasource.username=bankuser
spring.datasource.password=bankpassword
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update # Создание/обновление схемы БД при запуске
spring.jpa.show-sql=true # Показывать SQL запросы в логах
spring.jpa.properties.hibernate.format_sql=true # Форматировать SQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```
**Обязательно измените** значения `spring.datasource.*` на соответствующие вашей установке PostgreSQL.

Схема базы данных будет создана или обновлена автоматически при первом запуске приложения благодаря `ddl-auto=update`.

### Запуск приложения

После успешной сборки и настройки подключения к базе данных вы можете запустить приложение.

Используя Maven:

```bash
mvn spring-boot:run
```
Или запустив JAR-файл (после `mvn clean package`):

```bash
java -jar target/bankapi-model.jar
```
(Убедитесь, что имя JAR файла совпадает с тем, которое получилось после сборки, например, `target/ваше-название-проекта.jar`)

Приложение запустится и будет доступно по адресу `http://localhost:8080` (если порт не изменен в конфигурации).

## 📂 Структура базы данных

Приложение использует две основные таблицы:

### Таблица счетов (`bank_account`)
Содержит информацию о банковских счетах пользователей.

| Название поля | Тип данных в БД | Тип данных в Java | Описание           |
|---------------|-----------------|-------------------|--------------------|
| `user_id`     | `BIGINT`        | `Long`            | Уникальный ID пользователя (Primary Key), генерируется автоматически. |
| `balance`     | `NUMERIC(38,2)` | `BigDecimal`      | Текущий баланс счета. |

**Пример тестовых данных:**

| user_id | balance |
|---------|---------|
| 1       | 1000.00 |
| 2       | 500.00  |
| 3       | 0.00    |

### Таблица истории операций (`bank_transaction`)
Содержит записи обо всех транзакциях (пополнение, снятие, перевод).

| Название поля     | Тип данных в БД | Тип данных в Java | Описание           |
|-------------------|-----------------|-------------------|--------------------|
| `id`              | `BIGINT`        | `Long`            | Уникальный ID транзакции (Primary Key), генерируется автоматически. |
| `user_id`         | `BIGINT`        | `Long`            | ID пользователя, которому принадлежит эта транзакция. |
| `amount`          | `NUMERIC(38,2)` | `BigDecimal`      | Сумма операции. |
| `type`            | `VARCHAR`       | `String`          | Тип операции (например, `DEPOSIT`, `WITHDRAW`, `TRANSFER_OUT`, `TRANSFER_IN`). |
| `timestamp`       | `TIMESTAMP`     | `LocalDateTime`   | Время выполнения операции. |
| `balance_after`   | `NUMERIC(38,2)` | `BigDecimal`      | Баланс счета `user_id` после выполнения этой операции. |
| `related_user_id` | `BIGINT`        | `Long`            | **(Новое поле)** Для операций перевода (`TRANSFER_OUT`, `TRANSFER_IN`) - ID связанного пользователя (получателя или отправителя). `NULL` для `DEPOSIT` и `WITHDRAW`. |

**Пример тестовых данных (после нескольких операций, включая перевод):**

| id  | user_id | amount | type        | timestamp           | balance_after | related_user_id |
|-----|---------|--------|-------------|---------------------|---------------|-----------------|
| 1   | 1       | 200.00 | DEPOSIT     | 2024-04-01 12:30:00 | 1200.00       | NULL            |
| 2   | 2       | 50.00  | WITHDRAW    | 2024-04-01 13:00:00 | 450.00        | NULL            |
| 3   | 1       | 100.00 | TRANSFER_OUT| 2024-04-02 09:00:00 | 1100.00       | 2               |
| 4   | 2       | 100.00 | TRANSFER_IN | 2024-04-02 09:00:00 | 550.00        | 1               |
| 5   | 1       | 50.00  | WITHDRAW    | 2024-04-03 10:00:00 | 1050.00       | NULL            |

## 🌐 API Эндпоинты

Базовый URL для всех эндпоинтов: `/api/bank`

### 1️⃣ Получение баланса

* **URL:** `/api/bank/balance/{userId}`
* **Метод:** `GET`
* **Описание:** Возвращает текущий баланс счета пользователя.
* **Параметры пути:**
    * `userId` (`Long`): ID пользователя.

**Пример успешного запроса (curl):**

```bash
curl -X GET http://localhost:8080/api/bank/balance/1
```

**Пример успешного запроса (PowerShell - Invoke-RestMethod):**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/balance/1" -Method Get
```

**Пример успешного ответа (JSON):**

```json
{
  "userId": 1,
  "balance": 1000.00
}
```
*(Формат баланса может отличаться, например `1000.0`)*

**Пример ответа при пользователе не найден (JSON):**

```json
{
  "status": 0,
  "message": "Пользователь с ID 99 не найден"
}
```
*(Текст сообщения может варьироваться в зависимости от реализации @ExceptionHandler)*

### 2️⃣ Пополнение счета

* **URL:** `/api/bank/deposit`
* **Метод:** `POST`
* **Описание:** Пополняет баланс счета пользователя на указанную сумму.
* **Параметры запроса (Query Parameters или Form Data - application/x-www-form-urlencoded):**
    * `userId` (`Long`): ID пользователя.
    * `amount` (`BigDecimal`): Сумма пополнения (должна быть > 0).

**Пример успешного запроса (curl с Query Parameters):**

```bash
curl -X POST "http://localhost:8080/api/bank/deposit?userId=1&amount=200.50"
```

**Пример успешного запроса (curl с Form Data):**

```bash
curl -X POST http://localhost:8080/api/bank/deposit \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userId=1&amount=200.50"
```

**Пример успешного запроса (PowerShell):**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/deposit?userId=1&amount=200.50" -Method Post
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Баланс успешно пополнен"
}
```

**Пример ответа при ошибке (JSON):**

```json
{
  "status": 0,
  "message": "Пользователь с ID 99 не найден"
}
```
*(Или "Сумма пополнения должна быть положительной")*

### 3️⃣ Снятие средств

* **URL:** `/api/bank/withdraw`
* **Метод:** `POST`
* **Описание:** Снимает указанную сумму со счета пользователя.
* **Параметры запроса (Query Parameters или Form Data - application/x-www-form-urlencoded):**
    * `userId` (`Long`): ID пользователя.
    * `amount` (`BigDecimal`): Сумма снятия (должна быть > 0).

**Пример успешного запроса (curl):**

```bash
curl -X POST "http://localhost:8080/api/bank/withdraw?userId=1&amount=50.00"
```

**Пример успешного запроса (PowerShell):**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/withdraw?userId=1&amount=50.00" -Method Post
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Операция успешна"
}
```

**Пример ответа при ошибке (JSON):**

```json
{
  "status": 0,
  "message": "Недостаточно средств для пользователя ID: 1"
}
```
*(Или "Пользователь с ID 99 не найден", "Сумма снятия должна быть положительной")*

### 4️⃣ Перевод средств между счетами

* **URL:** `/api/bank/transfer`
* **Метод:** `POST`
* **Описание:** Переводит указанную сумму с одного счета на другой. Операция выполняется атомарно.
* **Тело запроса (application/json):** Объект JSON с полями:
    * `senderId` (`Long`): ID пользователя-отправителя.
    * `receiverId` (`Long`): ID пользователя-получателя.
    * `amount` (`BigDecimal`): Сумма перевода (должна быть > 0, отправитель и получатель не должны совпадать).

**Пример успешного запроса (curl):**

```bash
curl -X POST http://localhost:8080/api/bank/transfer \
-H "Content-Type: application/json" \
-d '{"senderId": 1, "receiverId": 2, "amount": 100.00}'
```

**Пример успешного запроса (PowerShell):**

```powershell
$body = @{
    senderId = 1
    receiverId = 2
    amount = 100.00
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/bank/transfer" -Method Post -ContentType "application/json" -Body $body
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Перевод выполнен успешно"
}
```

**Пример ответа при ошибке (JSON):**

```json
{
  "status": 0,
  "message": "Недостаточно средств для пользователя ID: 1"
}
```
*(Или "Sender user not found with ID: ...", "Receiver user not found with ID: ...", "Cannot transfer money to yourself.", "Transfer amount must be positive.")*

### 5️⃣ Получить историю транзакций

* **URL:** `/api/bank/transactions/{userId}`
* **Метод:** `GET`
* **Описание:** Возвращает список транзакций для указанного пользователя, отсортированных по времени. Можно фильтровать по диапазону дат и времени.
* **Параметры пути:**
    * `userId` (`Long`): ID пользователя.
* **Параметры запроса (Query Parameters, опционально):**
    * `startDate` (`LocalDateTime` в формате ISO 8601, например `2024-04-01T00:00:00`): Начальная дата и время диапазона (включая).
    * `endDate` (`LocalDateTime` в формате ISO 8601, например `2024-04-30T23:59:59`): Конечная дата и время диапазона (в зависимости от реализации запроса в репозитории может быть как включающим, так и не включающим).

**Пример запроса (получить все операции для пользователя 1):**

```bash
curl -X GET "http://localhost:8080/api/bank/transactions/1"
```

**Пример запроса (получить операции для пользователя 1 в диапазоне дат):**

```bash
curl -X GET "http://localhost:8080/api/bank/transactions/1?startDate=2024-04-01T00:00:00&endDate=2024-04-30T23:59:59"
```

**Пример запроса (PowerShell с Query Parameters):**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/bank/transactions/1?startDate=2024-04-01T00%3A00%3A00&endDate=2024-04-30T23%3A59%3A59" -Method Get
```
(В команде `Invoke-RestMethod` двоеточия в параметрах даты/времени нужно закодировать как `%3A`).

**Пример успешного ответа (JSON - массив объектов Transaction):**

```json
[
  {
    "id": 1,
    "userId": 1,
    "amount": 200.00,
    "type": "DEPOSIT",
    "timestamp": "2024-04-01T12:30:00",
    "balanceAfter": 1200.00,
    "relatedUserId": null
  },
  {
    "id": 3,
    "userId": 1,
    "amount": 100.00,
    "type": "TRANSFER_OUT",
    "timestamp": "2024-04-02T09:00:00",
    "balanceAfter": 1100.00,
    "relatedUserId": 2
  },
  {
    "id": 5,
    "userId": 1,
    "amount": 50.00,
    "type": "WITHDRAW",
    "timestamp": "2024-04-03T10:00:00",
    "balanceAfter": 1050.00,
    "relatedUserId": null
  }
  // ... другие операции пользователя 1 за указанный период ...
]
```
*(Порядок транзакций может отличаться, если не добавлена явная сортировка в репозитории/сервисе. В примере выше сортировка по timestamp.)*
*(Поле `relatedUserId` будет `null` для DEPOSIT/WITHDRAW и ID связанного пользователя для TRANSFERS.)*

**Пример ответа при пользователе не найден (JSON):**

```json
{
  "status": 0,
  "message": "Пользователь с ID 99 не найден"
}
```
*(Если в сервисе реализована проверка существования пользователя)*

## ✅ Запуск тестов

Для запуска всех юнит-тестов (`BankServiceTest`) и интеграционных тестов (`BankControllerIntegrationTest`) используйте ваш менеджер зависимостей:

С помощью Maven:

```bash
mvn test
```

С помощью Gradle:

```bash
./gradlew test
```
Интеграционные тесты используют базу данных H2 в памяти, сконфигурированную в `src/test/resources/application-test.properties`, поэтому для их запуска не требуется работающий внешний сервер PostgreSQL.

## 💾 Дамп базы данных

В корне этого репозитория будет находиться файл `bank_dump.sql`. Этот файл содержит SQL-команды для создания структуры таблиц `bank_account` и `bank_transaction` и заполнения их начальными тестовыми данными, как описано в разделе "Структура базы данных".

Вы можете использовать его для быстрого развертывания тестовой базы данных PostgreSQL. Пример команды для восстановления дампа (потребуется установленный клиент `psql`):

```bash
psql -U your_username -d your_database_name -h your_host -p your_port -f bank_dump.sql
```
**Обязательно замените** `your_username`, `your_database_name`, `your_host`, `your_port` на актуальные данные вашей установки PostgreSQL.
## 📷 Скриншоты
Структура базы данных:  
![DB Structure](screenshots/db_structure.png)