# exp-accounts

> Experience layer service that provides account management journeys for frontend applications

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [API Endpoints](#api-endpoints)
- [Domain SDK Dependencies](#domain-sdk-dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Overview

`exp-accounts` is the experience-layer (BFF) service that exposes account management capabilities to frontend applications. It provides a simplified API surface for common account operations including opening accounts, viewing account summaries, listing customer accounts, and closing accounts.

Unlike domain services that orchestrate complex multi-step sagas, `exp-accounts` focuses on composing domain service calls into frontend-friendly responses. It delegates all business orchestration to `domain-banking-accounts` and transforms responses into experience-layer DTOs optimized for UI consumption.

## Architecture

```
Frontend / Mobile App
         |
         v
exp-accounts  (port 8100)
         |
         +---> AccountExperienceService
         |             |
         |             v
         +---> domain-banking-accounts-sdk
                       |
                       +---> AccountsApi
                       +---> AccountBackofficeApi
```

## Module Structure

| Module | Purpose |
|--------|---------|
| `exp-accounts-interfaces` | Experience-layer DTOs: `OpenAccountRequest`, `OpenAccountResponse`, `AccountSummaryResponse` |
| `exp-accounts-core` | Service interfaces and implementations: `AccountExperienceService`, `AccountExperienceServiceImpl` |
| `exp-accounts-infra` | `DomainBankingAccountsClientFactory` (AccountsApi, AccountBackofficeApi) and `@ConfigurationProperties` |
| `exp-accounts-web` | `AccountsController`, Spring Boot application class, `application.yaml` |
| `exp-accounts-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## API Endpoints

Base path: `/api/v1/accounts`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/api/v1/accounts` | Open a new account | `201 Created` with `OpenAccountResponse` |
| `GET` | `/api/v1/accounts/{accountId}` | Get account summary | `200 OK` with `AccountSummaryResponse` |
| `GET` | `/api/v1/accounts/party/{partyId}` | Get all accounts for a party | `200 OK` with `Flux<AccountSummaryResponse>` |
| `DELETE` | `/api/v1/accounts/{accountId}` | Close an account | `204 No Content` |

### Request/Response DTOs

**OpenAccountRequest:**
```json
{
  "partyId": "uuid",
  "productId": "uuid",
  "productCatalogId": "uuid",
  "roleInContractId": "uuid",
  "accountType": "SAVINGS",
  "currency": "EUR",
  "branchId": "uuid"
}
```

**OpenAccountResponse:**
```json
{
  "accountId": "uuid",
  "contractId": "uuid",
  "executionId": "uuid",
  "status": "COMPLETED"
}
```

**AccountSummaryResponse:**
```json
{
  "accountId": "uuid",
  "accountNumber": "string",
  "accountType": "SAVINGS",
  "status": "ACTIVE",
  "currency": "EUR"
}
```

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-banking-accounts-sdk` | `DomainBankingAccountsClientFactory` | `AccountsApi`, `AccountBackofficeApi` | Open accounts, query summaries, close accounts |

## Configuration

```yaml
server:
  port: ${SERVER_PORT:8100}

firefly:
  cqrs:
    enabled: true
    command.timeout: 30s
    query:
      timeout: 15s
      caching-enabled: true
      cache-ttl: 5m

api-configuration:
  domain-platform:
    banking-accounts:
      base-path: ${BANKING_ACCOUNTS_URL:http://localhost:8090}
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8100` | HTTP server port |
| `BANKING_ACCOUNTS_URL` | `http://localhost:8090` | Base URL for `domain-banking-accounts` |

## Running Locally

```bash
# Prerequisites: ensure domain-banking-accounts is running
cd exp-accounts
mvn spring-boot:run -pl exp-accounts-web
```

Server starts on port `8100`. Swagger UI: [http://localhost:8100/swagger-ui.html](http://localhost:8100/swagger-ui.html)

Swagger UI is disabled in the `prod` profile.

## Testing

```bash
mvn clean verify
```

Tests cover `AccountExperienceServiceImpl` (unit tests with mocked domain SDK) and `AccountsController` (WebTestClient-based integration tests).

---

## Spring Profiles

| Profile | Logging | Swagger | Notes |
|---------|---------|---------|-------|
| `default` | INFO | Enabled | Standard development |
| `dev` | DEBUG | Enabled | Verbose debugging |
| `prod` | INFO | Disabled | Production |
| `openapi` | WARN | Enabled (port 18080) | OpenAPI spec generation |
