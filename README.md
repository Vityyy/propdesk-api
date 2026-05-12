<p align="right">
  <strong>English</strong> | <a href="README.es.md">Español</a>
</p>

# Property Management Back Office API

<div align="center">

![Java](https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Tests](https://img.shields.io/badge/tests-18%20passing-22C55E?style=for-the-badge)
![Status](https://img.shields.io/badge/status-portfolio_case_study-2563EB?style=for-the-badge)

**Spring Boot backend for property owners and rental administrators.**

</div>

---

## Overview

Property Management Back Office API is a backend case study for managing rental properties, apartments, tenants, payments, expenses, and delegated administrator access.

The project started as university work and is presented here as a backend showcase, not as a production SaaS. The focus is on fundamentals: relational modeling, authentication, scoped authorization, transactional business rules, database migrations, Docker, CI, tests and, of course, languages and frameworks.

---

## Highlights

- Layered Spring Boot REST API: controllers, services, repositories, DTOs, JPA entities
- JWT authentication with Spring Security OAuth2 Resource Server
- Refresh token stored in an `HttpOnly` cookie
- `OWNER` and `ADMIN` roles with scoped resource access
- Owner-to-admin association flow with explicit admin acceptance
- Property creation with apartment generation from floor/number ranges
- Tenant assignment, vacancy handling, expenses, rent status, and maintenance fees
- Payment ledger used for monthly revenue, expense, commission, and profit summaries
- Soft deletes for properties and apartments
- Flyway migrations with Hibernate schema validation
- Docker Compose with PostgreSQL health checks
- GitHub Actions build/test pipeline
- 18 passing tests across Cucumber, integration, and unit tests

---

## Architecture

```mermaid
flowchart LR
    Client["Frontend / API Client"] --> Security["Spring Security<br/>JWT Resource Server"]
    Security --> Controller["REST Controllers<br/>DTO boundary"]
    Controller --> Service["Service Layer<br/>transactions + business rules"]
    Service --> Authz["Ownership Checks<br/>OWNER / ADMIN scope"]
    Service --> Repository["Spring Data JPA<br/>repositories + JPQL"]
    Repository --> Database[("PostgreSQL")]
    Flyway["Flyway migrations"] --> Database

    classDef edge fill:#111827,stroke:#928dd3,color:#f9fafb
    classDef core fill:#172033,stroke:#60a5fa,color:#f9fafb
    classDef rule fill:#1f2937,stroke:#f59e0b,color:#f9fafb
    classDef db fill:#052e2b,stroke:#10b981,color:#f9fafb

    class Client,Security edge
    class Controller,Service,Repository core
    class Authz rule
    class Database,Flyway db
```

| Layer | Responsibility |
|---|---|
| Controllers | REST endpoints and DTO mapping |
| Services | Transactions, business rules, ownership checks |
| Repositories | Spring Data JPA queries |
| Entities | Relational domain model |
| Config | Security, JWT, CORS, logging, OpenAPI toggles |

---

## Domain

The model separates users from rental resources. `Admin` and `Owner` inherit from `User`; owners manage properties; properties contain apartments; apartments can have tenants, expenses, maintenance fees, and payment records.

```mermaid
classDiagram
    direction LR

    class User {
        UUID id
        String name
        String password
        Role role
    }
    class Admin
    class Owner {
        BigDecimal adminCut
        Boolean adminAssociationAccepted
    }
    class Property {
        UUID id
        String name
        String address
        URL imageUrl
        boolean isDeleted
    }
    class Apartment {
        UUID id
        int number
        int floor
        BigDecimal rent
        PaymentStatus paymentStatus
        boolean isDeleted
    }
    class Tenant {
        UUID id
        String name
        String phone
        String email
    }
    class Payment {
        UUID id
        BigDecimal amount
        PaymentType type
        int billingMonth
        int billingYear
        boolean isCancelled
    }
    class Expense
    class MaintenanceFee

    User <|-- Admin
    User <|-- Owner
    Admin "0..1" --> "*" Owner : accepted management
    Owner "1" --> "*" Property : owns
    Property "1" --> "*" Apartment : contains
    Apartment "*" --> "0..1" Tenant : rented by
    Apartment "1" --> "*" Expense : records
    Apartment "1" --> "*" MaintenanceFee : recurring fees
    Apartment "1" --> "*" Payment : ledger entries
```

---

## Screenshots

### Summary Dashboard

<p align="center">
  <img src="./assets/media/summary.jpg" alt="Financial summary dashboard" width="1000"/>
  <br/>
  <small><em>Financial summary dashboard</em></small>
</p>

### Properties Dashboard

<p align="center">
  <img src="./assets/media/properties.gif" alt="Properties dashboard" width="1000"/>
  <br/>
  <small><em>Properties dashboard</em></small>
</p>

### Apartment Grid

<p align="center">
  <img src="./assets/media/apartment.gif" alt="Apartment grid" width="1000"/>
  <br/>
  <small><em>Apartment grid grouped by property and floor</em></small>
</p>

### Tenants Table

<p align="center">
  <img src="./assets/media/tenants.jpg" alt="Tenants table" width="1000"/>
  <br/>
  <small><em>Tenants table</em></small>
</p>

### Maintenance Fees

<p align="center">
  <img src="./assets/media/maintenance.jpg" alt="Maintenance fees screen" width="1000"/>
  <br/>
  <small><em>Maintenance fees by category and apartment</em></small>
</p>

### Reports

<p align="center">
  <img src="./assets/media/reports.jpg" alt="Reports screen" width="1000"/>
  <br/>
  <small><em>Reports and export screen</em></small>
</p>

---

## Backend Decisions

### Consent-based admin access

Owners can request association with an admin and define the admin commission percentage. The admin cannot manage the owner immediately: the request must be accepted first.

That rule is enforced in service methods, not only in the frontend. Owners can only access their own resources, and admins can only access owners who accepted them.

### Payment ledger

The system stores rent, expense, and maintenance-fee events as `Payment` records. Marking an apartment as paid creates current-month rent and maintenance-fee records; marking it unpaid cancels the active records.

This makes the summary endpoint depend on persisted financial events instead of recalculating everything from current apartment state.

### Flyway migrations

The schema is versioned with Flyway:

```text
src/main/resources/db/migration/V1__init_schema.sql
```

Hibernate runs with `ddl-auto=validate`, so the app fails if the database schema does not match the entity model. `baseline-on-migrate=true` is enabled so an existing Render database can be adopted safely after introducing Flyway.

### OpenAPI

Springdoc OpenAPI is included but disabled by default on public deployments.

```env
OPENAPI_ENABLED=false
SWAGGER_UI_ENABLED=false
```

OpenAPI is the machine-readable API contract. Swagger UI is the browser interface generated from it. For local/private demos, enabling both exposes:

```text
/v3/api-docs
/swagger-ui/index.html
```

---

## Security

- Stateless Spring Security configuration
- Password hashing through Spring Security `PasswordEncoder`
- JWT access tokens with subject and role claims
- Refresh token stored as an `HttpOnly` cookie
- CORS configured from explicit environment origins
- Generic login errors to avoid username/password probing
- Request logging configured without payloads to avoid credential logging
- Secrets and database credentials loaded from environment variables

---

## Testing

Verified locally on May 11, 2026:

```text
18 tests, 0 failures, 0 errors, 0 skipped
```

Covered flows include registration, login, property/apartment creation, owner-admin association, denied admin access before acceptance, accepted admin access, maintenance-fee payment generation/cancellation, and monthly financial summary calculation.

---

## Stack

| Area | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security, OAuth2 Resource Server, JWT |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 17, H2 for tests |
| Migrations | Flyway |
| Testing | JUnit 5, Cucumber, AssertJ, Mockito |
| Build | Gradle Kotlin DSL |
| Infra | Docker, Docker Compose, Render |
| CI | GitHub Actions |
| API docs | Springdoc OpenAPI, environment-controlled |

---

## Local Development

Create `.env` from `.env.example`, then run:

```powershell
docker compose up --build
```

Backend URL:

```text
http://localhost:8080
```

Run tests:

```powershell
.\gradlew.bat test
```

---

## Main API Surface

| Area | Endpoints |
|---|---|
| Auth | `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/register/admin`, `/auth/register/owner` |
| Admins | `/admins`, `/admins/me/owners`, `/admins/me/owner-requests` |
| Owners | `/owners/me/admin`, `/owners/{ownerId}/summary` |
| Properties | `/properties`, `/properties/{propertyId}`, `/properties/{propertyId}/apartments` |
| Apartments | `/apartments`, `/apartments/single`, `/apartments/bulk`, `/apartments/{apartmentId}` |
| Tenants | `/apartments/{apartmentId}/tenant` |
| Expenses | `/apartments/{apartmentId}/expenses` |
| Maintenance fees | `/apartments/{apartmentId}/maintenance-fees` |

---

## Current Limitations

This is not presented as production SaaS. The next backend improvements would be:

- Split access and refresh token signing secrets
- Expand DTO validation and normalize error responses
- Add rate limiting to authentication endpoints
- Add Testcontainers PostgreSQL tests for migrations and database-specific queries
- Add structured audit logs for security-sensitive actions

---

<div align="center">

[![LinkedIn](https://img.shields.io/badge/LinkedIn-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/camilosassone/)
[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:camilosassone.dev@gmail.com)

</div>
