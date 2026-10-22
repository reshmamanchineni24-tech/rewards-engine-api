
# Rewards Engine REST API

A Spring Boot application that calculates customer reward points based on purchase transactions. The API provides monthly reward summaries, total points earned, and transaction history for individual customers.

## Technologies

- Java 17+
- Spring Boot
- Spring Data JPA
- Maven
- H2 In-Memory Database
- Swagger UI / OpenAPI
- JUnit 5 and Mockito

## Reward Points Calculation

Customers earn points based on the amount spent in each transaction:

- 1 point for every whole dollar spent above $50, up to $100.
- 2 points for every whole dollar spent above $100.
- No points for purchases of $50 or less.

Only whole-dollar amounts are considered when calculating points.

| Purchase Amount | Reward Points |
|-----------------|---------------|
| $50 | 0 |
| $75 | 25 |
| $100 | 50 |
| $120 | 90 |
| $140 | 130 |

For example, a purchase of $120 earns 50 points for the amount between $50 and $100, plus 40 points for the additional $20.

## Running the Application

### Prerequisites

- JDK 17 or higher
- Maven Wrapper (included in the project)

### Start the application

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

The application runs on:

http://localhost:8080

If port 8080 is already in use, start it on port 8081:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Use the selected port in the URLs below.

## API Documentation

Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI specification:

http://localhost:8080/v3/api-docs

Swagger UI allows users to execute API requests and inspect the responses.

## Database Configuration

The application uses an H2 in-memory database.

H2 Console:

http://localhost:8080/h2-console

Database connection:

- JDBC URL: `jdbc:h2:mem:rewardsdb`
- Username: `sa`
- Password: Leave blank

Database tables are initialized through `schema.sql`, and sample transactions are loaded from `data.sql`.

## Sample Transaction Data

The database contains:

- 6 customers
- 47 purchase transactions
- Transactions dated July 8, 2026, through October 3, 2026

Each transaction includes:

- Transaction ID
- Customer ID
- Customer Name
- Transaction Amount
- Transaction Date

Sample customer:

| Customer ID | Customer Name |
|-------------|---------------|
| 1001 | Aarav Sharma |

## REST API Endpoints

### 1. Get Rewards for All Customers

```http
GET /rewards
```

Returns reward summaries for customers with transactions in the selected date range.

Example:

```text
http://localhost:8080/rewards?startDate=2026-07-06&endDate=2026-10-06
```

### 2. Get Rewards for a Customer

```http
GET /rewards/{customerId}
```

Returns the monthly reward points and total reward points earned by a specific customer.

Example:

```text
http://localhost:8080/rewards/1001?startDate=2026-07-06&endDate=2026-10-06
```

Sample response:

```json
{
  "customerId": 1001,
  "totalPoints": 964,
  "monthlyRewards": [
    {
      "month": "2026-07",
      "points": 370
    },
    {
      "month": "2026-08",
      "points": 210
    },
    {
      "month": "2026-09",
      "points": 224
    },
    {
      "month": "2026-10",
      "points": 160
    }
  ]
}
```

### 3. Get Customer Transactions

```http
GET /transactions/{customerId}
```

Returns all recorded transactions for a customer, ordered by transaction date with the newest transactions first.

Example:

```text
http://localhost:8080/transactions/1001
```

Sample response:

```json
[
  {
    "id": 9,
    "customerId": 1001,
    "customerName": "Aarav Sharma",
    "transactionAmount": 155.00,
    "transactionDate": "2026-10-02"
  },
  {
    "id": 8,
    "customerId": 1001,
    "customerName": "Aarav Sharma",
    "transactionAmount": 142.53,
    "transactionDate": "2026-09-21"
  }
]
```

## Date Range Filtering

The rewards endpoints support optional query parameters:

| Parameter | Description |
|-----------|-------------|
| `startDate` | Beginning of the date range, inclusive |
| `endDate` | End of the date range, inclusive |

Dates use the format `yyyy-MM-dd`.

If no dates are provided, the API uses a three-month window ending on the current date.

For testing the complete sample dataset, use:

```text
startDate=2026-07-06&endDate=2026-10-06
```

## Error Handling

The API provides HTTP error responses for invalid requests.

| HTTP Status | Description |
|-------------|-------------|
| 200 | Request processed successfully |
| 400 | Invalid customer ID or date parameters |
| 404 | Customer transactions not found |
| 405 | HTTP method not supported |
| 500 | Unexpected server error |

Error responses use the Problem Details format.

## Testing

The project includes unit and integration tests for its API controllers, services, repository, and reward calculation logic.

Run the tests:

Windows:

```powershell
.\mvnw.cmd clean test
```

macOS / Linux:

```bash
./mvnw clean test
```

The project has been validated with 53 passing tests.

## Implementation Details

- Reward points are calculated from individual purchase amounts.
- Monetary values use `BigDecimal` to maintain precision.
- Customer transactions are retrieved using Spring Data JPA.
- Reward calculations are separated from database access.
- The database uses SQL initialization scripts.
- The application provides interactive API documentation through Swagger UI.
- Customer names are included in transaction API responses.

## Repository

GitHub: https://github.com/reshmamanchineni24-tech/rewards-engine-api
