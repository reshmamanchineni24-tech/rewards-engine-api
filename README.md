# retailer-rewards-api

Spring Boot REST API that calculates reward points per customer, per month and in total, from a record of purchases.

## Reward rules

For each transaction:

- 2 points for every dollar spent **over $100**
- 1 point for every dollar spent **between $50 and $100**
- nothing for the first $50

Points are earned per whole dollar, so the amount is truncated first ($120.97 is treated as $120).

| Purchase | Calculation | Points |
|----------|-------------|--------|
| $120 | 2 x $20 + 1 x $50 | 90 |
| $140 | 2 x $40 + 1 x $50 | 130 |
| $75 | 1 x $25 | 25 |
| $50 | | 0 |

## Running it

Requires JDK 17+.

```bash
./mvnw spring-boot:run     # Windows: .\mvnw.cmd spring-boot:run
./mvnw test
```

The app starts on http://localhost:8080 with an in-memory H2 database seeded from `src/main/resources/data.sql`.

| What | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console (`jdbc:h2:mem:rewardsdb`, user `sa`, empty password) |

The sample data (6 customers, 47 purchases) is dated **2026-07-08 to 2026-10-03**, so the window
`startDate=2026-07-06&endDate=2026-10-06` covers all of it. The default window (no parameters) is the three months
ending today, so it only matches the sample data while today is within about three months of those dates.

## Using Swagger UI

1. Open http://localhost:8080/swagger-ui.html.
2. Expand **Rewards** > `GET /rewards/{customerId}` and click **Try it out**.
3. Enter `customerId` = `1001`.
4. Click **Execute**.

`startDate` and `endDate` are optional. Swagger pre-fills them with `2026-07-06` and `2026-10-06`, the window that
covers all of the sample data, so you get results straight away. Clear both fields to use the default window instead.

## API

### `GET /rewards`

Rewards for every customer with purchases in the window, ordered by customer id.

### `GET /rewards/{customerId}`

Rewards for one customer.

| Query parameter | Format | Default | Meaning |
|-----------------|--------|---------|---------|
| `startDate` | `yyyy-MM-dd` | three months before `endDate` | first day of the window, inclusive |
| `endDate` | `yyyy-MM-dd` | today | last day of the window, inclusive |

- A customer with no purchases at all returns `404`; a known customer with none in the window returns `0` points.
- Months are sorted ascending and listed when the customer had a purchase that month, even if it earned 0 points.

**Examples**

```bash
# explicit window: the last three months of sample data
curl "http://localhost:8080/rewards/1001?startDate=2026-07-06&endDate=2026-10-06"

# all customers in the same window
curl "http://localhost:8080/rewards?startDate=2026-07-06&endDate=2026-10-06"

# only startDate: from that day until today
curl "http://localhost:8080/rewards?startDate=2026-08-01"

# only endDate: the three months before that day
curl "http://localhost:8080/rewards?endDate=2026-10-06"

# no dates: the three months ending today
curl "http://localhost:8080/rewards/1001"
```

**Response** (`200 OK`)

```json
{
  "customerId": 1001,
  "totalPoints": 964,
  "monthlyRewards": [
    { "month": "2026-07", "points": 370 },
    { "month": "2026-08", "points": 210 },
    { "month": "2026-09", "points": 224 },
    { "month": "2026-10", "points": 160 }
  ]
}
```

### `GET /transactions/{customerId}`

All transactions for a customer, newest first.

```json
[
  { "id": 9, "customerId": 1001, "transactionAmount": 155.00, "transactionDate": "2026-10-02" },
  { "id": 8, "customerId": 1001, "transactionAmount": 142.53, "transactionDate": "2026-09-21" }
]
```

### Errors

Errors use RFC 7807 problem details.

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Start date 2026-10-01 must not be after end date 2026-09-01",
  "instance": "/rewards"
}
```

| Status | When |
|--------|------|
| 400 | non-numeric or non-positive customer id, malformed date, `startDate` after `endDate` |
| 404 | customer has no transactions |
| 405 | anything other than GET |
| 500 | anything unexpected (details are logged, not returned) |

## Design notes

- Amounts are `BigDecimal` / `DECIMAL(12,2)`, never floating point. Points and totals are `long`.
- The point rules live in `RewardPointsCalculator`, a pure function with its own parameterised tests.
- The date window is filtered in the database (indexed), not in memory, and "today" comes from an injected `Clock` so tests are deterministic.
- Schema is owned by `schema.sql`; Hibernate DDL is off.
