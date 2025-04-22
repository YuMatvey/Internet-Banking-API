# Bank API

Простое RESTful API для базовых операций с банковскими счетами: просмотр баланса, пополнение, снятие, перевод средств и получение истории транзакций.

## Используемые технологии

* **Spring Boot:** Фреймворк для быстрого создания Spring-приложений.
* **Spring Data JPA:** Упрощает работу с базами данных через JPA и Hibernate.
* **Hibernate:** Реализация ORM (Object-Relational Mapping).
* **PostgreSQL:** Реляционная база данных (используется в production).
* **H2 Database:** In-memory база данных (используется для интеграционных тестов).
* **Maven / Gradle:** Инструмент для сборки и управления зависимостями.
* **JUnit 5:** Фреймворк для написания юнит-тестов.
* **Mockito:** Фреймворк для создания мок-объектов в юнит-тестах.

## Настройка и запуск проекта

### Предварительные требования

* Установленная Java Development Kit (JDK) версии 17 или выше (рекомендуется JDK 21+).
* Установленный Apache Maven или Gradle.
* Запущенный экземпляр базы данных PostgreSQL.

### Сборка проекта

Склонируйте репозиторий проекта:

```bash
git clone <ссылка_на_ваш_репозиторий>
cd <папка_проекта>
```

Соберите проект с помощью Maven:

```bash
mvn clean package -DskipTests
```
Или с помощью Gradle:

```bash
./gradlew clean build -x test
```
`-DskipTests` или `-x test` пропускают выполнение тестов во время сборки. Если вы хотите запустить тесты, уберите этот флаг.

### Конфигурация базы данных

API настроено для работы с PostgreSQL. Параметры подключения к базе данных должны быть указаны в файле `src/main/resources/application.properties` (или `application.yml`):

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
**Обязательно обновите** эти значения, указав параметры вашей базы данных PostgreSQL. Схема базы данных будет создана автоматически при первом запуске приложения благодаря `ddl-auto=update`.

Для интеграционных тестов используется база данных H2 в памяти, сконфигурированная в `src/test/resources/application-test.properties`:

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect # Указываем диалект H2 для тестов
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Запуск приложения

После сборки и настройки базы данных, вы можете запустить приложение из командной строки:

```bash
java -jar target/bankapi-model.jar # Убедитесь, что имя JAR файла совпадает с вашим проектом
```
Приложение запустится на встроенном Tomcat сервере, обычно на порту 8080.

## API Эндпоинты

Базовый URL для всех эндпоинтов: `/api/bank`

### 1. Получить баланс счета

* **URL:** `/api/bank/balance/{userId}`
* **Метод:** `GET`
* **Описание:** Возвращает текущий баланс счета пользователя.
* **Параметры пути:**
    * `userId` (число, `Long`): ID пользователя.

**Пример успешного запроса:**

```bash
curl -X GET http://localhost:8080/api/bank/balance/1
```

**Пример успешного ответа (JSON):**

```json
{
  "balance": 1000.0,
  "userId": 1
}
```
*(Примечание: формат баланса может отличаться в зависимости от настройки сериализации BigDecimal, например, `1000.00`)*

**Пример ответа при пользователе не найден (JSON):**

```json
{
  "status": 0,
  "message": "Пользователь не найден"
}
```
*(Или аналогичный формат ошибки, настроенный в @ExceptionHandler)*

### 2. Пополнить баланс счета

* **URL:** `/api/bank/deposit`
* **Метод:** `POST`
* **Описание:** Пополняет баланс счета пользователя на указанную сумму.
* **Параметры запроса (application/x-www-form-urlencoded или query params):**
    * `userId` (число, `Long`): ID пользователя.
    * `amount` (число, `BigDecimal`): Сумма пополнения (должна быть положительной).

**Пример успешного запроса:**

```bash
curl -X POST "http://localhost:8080/api/bank/deposit?userId=1&amount=200.50"
```
Или с телом запроса (form-urlencoded):
```bash
curl -X POST http://localhost:8080/api/bank/deposit \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "userId=1&amount=200.50"
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Баланс успешно пополнен"
}
```

**Пример ответа при ошибке (пользователь не найден или некорректная сумма):**

```json
{
  "status": 0,
  "message": "Пользователь не найден"
}
```
*(Или "Сумма пополнения должна быть положительной", в зависимости от ошибки и настройки контроллера)*

### 3. Снять средства со счета

* **URL:** `/api/bank/withdraw`
* **Метод:** `POST`
* **Описание:** Снимает указанную сумму со счета пользователя.
* **Параметры запроса (application/x-www-form-urlencoded или query params):**
    * `userId` (число, `Long`): ID пользователя.
    * `amount` (число, `BigDecimal`): Сумма снятия (должна быть положительной).

**Пример успешного запроса:**

```bash
curl -X POST "http://localhost:8080/api/bank/withdraw?userId=1&amount=50.00"
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Операция успешна"
}
```

**Пример ответа при ошибке (недостаточно средств, пользователь не найден, некорректная сумма):**

```json
{
  "status": 0,
  "message": "Недостаточно средств"
}
```
*(Или "Пользователь не найден", "Сумма снятия должна быть положительной")*

### 4. Перевести средства между счетами

* **URL:** `/api/bank/transfer`
* **Метод:** `POST`
* **Описание:** Переводит указанную сумму с одного счета на другой.
* **Тело запроса (application/json):** Объект JSON с полями:
    * `senderId` (число, `Long`): ID отправителя.
    * `receiverId` (число, `Long`): ID получателя.
    * `amount` (число, `BigDecimal`): Сумма перевода (должна быть положительной).

**Пример успешного запроса:**

```bash
curl -X POST http://localhost:8080/api/bank/transfer \
-H "Content-Type: application/json" \
-d '{"senderId": 1, "receiverId": 2, "amount": 100.00}'
```

**Пример успешного ответа (JSON):**

```json
{
  "status": 1,
  "message": "Перевод выполнен успешно"
}
```

**Пример ответа при ошибке (недостаточно средств, пользователь не найден, некорректная сумма и т.д.):**

```json
{
  "status": 0,
  "message": "Недостаточно средств для пользователя ID: 1"
}
```
*(Или "Sender user not found...", "Cannot transfer money to yourself.", "Transfer amount must be positive.")*

### 5. Получить историю транзакций

* **URL:** `/api/bank/transactions/{userId}`
* **Метод:** `GET`
* **Описание:** Возвращает список транзакций для указанного пользователя. Можно фильтровать по диапазону дат.
* **Параметры пути:**
    * `userId` (число, `Long`): ID пользователя.
* **Параметры запроса (опционально):**
    * `startDate` (строка, `LocalDateTime` в формате ISO 8601, например `2023-10-27T10:00:00`): Начальная дата и время диапазона (включая).
    * `endDate` (строка, `LocalDateTime` в формате ISO 8601): Конечная дата и время диапазона (не включая, если используется `Before`, или включая, если используется `Between`).

**Пример запроса (все транзакции):**

```bash
curl -X GET http://localhost:8080/api/bank/transactions/1
```

**Пример запроса (транзакции за период):**

```bash
curl -X GET "http://localhost:8080/api/bank/transactions/1?startDate=2023-10-20T00:00:00&endDate=2023-10-27T23:59:59"
```

**Пример успешного ответа (JSON - массив транзакций):**

```json
[
  {
    "id": 101,
    "userId": 1,
    "amount": 100.00,
    "type": "DEPOSIT",
    "timestamp": "2023-10-20T10:30:00",
    "balanceAfter": 1100.00,
    "relatedUserId": null
  },
  {
    "id": 102,
    "userId": 1,
    "amount": 50.00,
    "type": "WITHDRAW",
    "timestamp": "2023-10-25T15:00:00",
    "balanceAfter": 1050.00,
    "relatedUserId": null
  },
  {
    "id": 103,
    "userId": 1,
    "amount": 100.00,
    "type": "TRANSFER_OUT",
    "timestamp": "2023-10-27T12:00:00",
    "balanceAfter": 950.00,
    "relatedUserId": 2
  }
  // ... другие транзакции ...
]
```
*(Формат даты и баланса может зависеть от настроек сериализации)*

**Пример ответа при пользователе не найден (JSON):**

```json
{
  "status": 0,
  "message": "Пользователь не найден"
}
```
*(Если в сервисе реализована проверка существования пользователя)*

## Запуск тестов

Вы можете запустить все юнит- и интеграционные тесты с помощью вашего менеджера зависимостей:

С помощью Maven:

```bash
mvn test
```

С помощью Gradle:

```bash
./gradlew test
```
Интеграционные тесты используют базу данных H2 в памяти, поэтому для их запуска не требуется внешний сервер PostgreSQL.

## Дамп базы данных

В корне этого репозитория находится файл `bank_dump.sql`. Этот файл содержит структуру базы данных PostgreSQL и несколько тестовых записей для счетов и транзакций.

Вы можете использовать его для быстрого развертывания тестовой базы данных. Пример команды для восстановления дампа (потребуется установленный клиент `psql`):

```bash
pg_restore -U your_username -d your_database_name -h your_host -p your_port bank_dump.sql
# Или с использованием psql, если дамп в текстовом формате:
# psql -U your_username -d your_database_name -h your_host -p your_port -f bank_dump.sql
```
**Обязательно замените** `your_username`, `your_database_name`, `your_host`, `your_port` на актуальные значения.
