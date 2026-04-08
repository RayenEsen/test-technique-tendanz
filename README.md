# Insurance Pricing Engine — Tendanz Group Technical Test

A fullstack insurance pricing engine built with Spring Boot 3.2 and Angular 17.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm

---

## Running the Backend

```bash
cd backend
mvn spring-boot:run
```

- API available at: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: *(leave empty)*

The database is in-memory (H2) and is automatically seeded with zones, products, and pricing rules on startup. No setup required.

---

## Running the Frontend

```bash
cd frontend
npm install
ng serve
```

- App available at: `http://localhost:4200`
- Make sure the backend is running first (CORS is configured for port 4200)

---

## Running Backend Tests

```bash
cd backend
mvn test
```

8 unit tests — all passing. Covers all age categories (YOUNG, ADULT, SENIOR, ELDERLY), error cases, and boundary conditions.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all insurance products |
| POST | `/api/quotes` | Create a new quote |
| GET | `/api/quotes` | List all quotes (optional filters: `productId`, `minPrice`) |
| GET | `/api/quotes?page=0&size=10` | Paginated quotes |
| GET | `/api/quotes/{id}` | Get quote by ID |
| GET | `/api/quotes/{id}/pdf` | Export quote as PDF |
| GET | `/api/quotes/{id}/history` | Get modification history |

---

## Pricing Formula

```
Final Price = Base Rate × Age Factor × Zone Coefficient
```

| Age Range | Category | Factor |
|-----------|----------|--------|
| 18 - 24   | YOUNG    | 1.30   |
| 25 - 45   | ADULT    | 1.00   |
| 46 - 65   | SENIOR   | 1.20   |
| 66 - 99   | ELDERLY  | 1.50   |

| Zone        | Code | Coefficient |
|-------------|------|-------------|
| Grand Tunis | TUN  | 1.20        |
| Sfax        | SFX  | 1.00        |
| Sousse      | SOU  | 1.10        |

| Product              | Base Rate (TND) |
|----------------------|-----------------|
| Assurance Auto       | 500.00          |
| Assurance Habitation | 300.00          |
| Assurance Santé      | 800.00          |
| Assurance Voyage     | 250.00          |

**Example:** Client aged 30, zone Tunis, Auto Insurance = 500 × 1.00 × 1.20 = **600.00 TND**

---

## What Was Implemented

### Backend
- `PricingService.calculateQuote()` — full pricing logic with `BigDecimal` precision
- `QuoteController` — 3 REST endpoints (POST, GET by ID, GET list with filters)
- `QuoteRepository` — custom queries: `findByClientName`, `findByProductId`, price threshold query
- `GlobalExceptionHandler` — centralized 400 / 404 / 500 error handling
- `CorsConfig` — allows Angular frontend on port 4200
- 8 unit tests in `PricingServiceTest`

### Frontend
- `QuoteService` and `ProductService` — HTTP calls with `catchError`
- Quote Form — Reactive Form with validation (required fields, age 18-99), product dropdown from API
- Quote List — table with sort by date/price, filter by product and min price, pagination
- Quote Detail — full pricing breakdown with applied rules

### Bonus
- Pagination — `GET /api/quotes?page=0&size=10` returns `PagedResponse`
- PDF Export — `GET /api/quotes/{id}/pdf` generates a styled PDF (OpenPDF)
- Quote History — `GET /api/quotes/{id}/history` tracks creation events
- 4th Product — Assurance Voyage (250 TND base rate, custom age factors)
