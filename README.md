# Secure M2M API Gateway – API Key Authentication Service

A production-oriented Spring Boot service designed to secure **Machine-to-Machine (M2M) communication** using API Key authentication, BCrypt secret verification, scope-based authorization, and correlation ID tracking.

## 🚀 Project Overview

This service provides a secure authentication layer for external systems and backend services communicating through REST APIs.

It uses a two-part API key structure:

- **Key ID:** Used to identify the API key record.
- **Secret:** A confidential credential verified using BCrypt.

The system authenticates incoming requests and authorizes access based on assigned scopes.

### Key Highlights

- API Key-based M2M authentication.
- BCrypt-based secret verification.
- Database indexed API key lookup.
- Scope-based authorization.
- API key expiration validation.
- Correlation ID tracking.
- Centralized RFC 7807 error responses.
- Spring Security integration.
- Stateless authentication architecture.

## 🏗️ System Architecture

```text
                 INCOMING M2M TRAFFIC
                          |
                          v
                HTTP Request
                X-API-KEY Header
                          |
                          v
             +------------------------+
             |  CorrelationIdFilter   |
             +------------------------+
                          |
                          v
             +------------------------+
             |    ApiKeyAuthFilter    |
             +------------------------+
                          |
              +-----------+-----------+
              |                       |
              v                       v
        Parse API Key          Database Lookup
        key_id + secret        using key_id
              |                       |
              +-----------+-----------+
                          |
                          v
                BCrypt.matches()
                Expiry Validation
                          |
                 +--------+--------+
                 |                 |
                 v                 v
             Authentication     Authentication
                Failed              Success
                 |                   |
                 v                   v
             HTTP 401          Map API Scopes
                                     |
                                     v
                             SecurityContextHolder
                                     |
                                     v
                             SecurityFilterChain
                                     |
                            +--------+--------+
                            |                 |
                            v                 v
                        HTTP 200           HTTP 403
                      Authorized         Forbidden
```

## 🔐 Authentication Flow

### 1. Incoming Request

External applications send requests with an API key header.

```http
GET /api/v1/orders
X-API-KEY: ak_live_xxxx.secret_yyyy
```

**Important:** The above key is a demonstration format, not a real credential.

### 2. CorrelationIdFilter

- Extracts the incoming `X-Correlation-ID`.
- Generates a new ID if one is not provided.
- Stores the ID in SLF4J MDC.
- Enables request-level log tracing.

### 3. ApiKeyAuthFilter

- Extracts the API key from the request header.
- Validates the API key format.
- Separates the Key ID and Secret.

### 4. Database Lookup

Uses the Key ID to identify the API key record.

```sql
SELECT *
FROM api_keys
WHERE key_id = ?;
```

A suitable database index allows efficient key lookup.

### 5. BCrypt Verification

The submitted secret is verified against the stored BCrypt hash.

```java
passwordEncoder.matches(
    rawSecret,
    storedSecretHash
);
```

The raw API secret is never stored in the database.

### 6. Expiration Validation

Checks whether the API key is expired, revoked, or otherwise inactive.

### 7. Scope Mapping

After successful authentication, the API key's assigned scopes are converted into Spring Security authorities.

Example:

```text
orders:read
orders:write
users:read
```

Spring Security authority representation:

```text
SCOPE_orders:read
SCOPE_orders:write
SCOPE_users:read
```

### 8. Authorization

The SecurityFilterChain checks whether the authenticated API key has permission to access the requested endpoint.

| Scenario | HTTP Status |
|---|---|
| Valid API key and required scope | 200 OK |
| Missing or invalid API key | 401 Unauthorized |
| Expired or revoked API key | 401 Unauthorized |
| Valid key but insufficient scope | 403 Forbidden |

## 🛡️ Security Features

| Feature | Description |
|---|---|
| API Key Authentication | Secures M2M communication |
| BCrypt Hashing | Protects API secrets at rest |
| Indexed Lookup | Efficient API key identification |
| Scope Authorization | Controls endpoint-level access |
| Expiration Validation | Prevents expired credentials from being used |
| Correlation ID | Supports request tracing |
| RFC 7807 | Standardized error responses |
| Stateless Security | No server-side session dependency |

## 📡 Example API Requests

### Authorized Request

```http
GET /api/v1/orders
X-API-KEY: ak_live_xxxx.secret_yyyy
X-Correlation-ID: req-123456
```

### Unauthorized Response – 401

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid API credentials",
  "instance": "/api/v1/orders"
}
```

### Forbidden Response – 403

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Insufficient permissions",
  "instance": "/api/v1/orders"
}
```

## 🗄️ Database Design

Suggested API key table:

```sql
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_id VARCHAR(100) NOT NULL UNIQUE,
    secret_hash VARCHAR(255) NOT NULL,
    client_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

For scope management, use a separate mapping table:

```sql
CREATE TABLE api_key_scopes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_key_id BIGINT NOT NULL,
    scope VARCHAR(100) NOT NULL,
    FOREIGN KEY (api_key_id)
        REFERENCES api_keys(id),
    UNIQUE (api_key_id, scope)
);
```

## 🧰 Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Web
- Spring JDBC / Database integration
- BCrypt Password Encoder
- MySQL
- SLF4J / Logback
- Maven
- REST APIs

*Adjust the stack list to match the dependencies actually used in the implementation.*

## ⚙️ Configuration

Keep sensitive configuration outside the application JAR.

Example environment configuration:

```properties
DB_URL=${DB_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
```

Never commit production API secrets, database passwords, or private credentials to GitHub.

## ▶️ Running the Project

### Prerequisites

- Java 21+
- Maven
- MySQL

### Clone Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd <PROJECT_DIRECTORY>
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

### Package JAR

```bash
mvn clean package
```

## 🧪 Testing Strategy

Important scenarios to cover:

- Valid API key authentication.
- Missing API key.
- Invalid API key format.
- Unknown Key ID.
- Incorrect secret.
- Expired API key.
- Revoked API key.
- Valid key with required scope.
- Valid key without required scope.
- Correlation ID generation.
- Correlation ID propagation.
- Concurrent API requests.
- Database failure handling.

## 🔮 Future Enhancements

- API key generation and rotation.
- Client-level rate limiting.
- API usage analytics.
- IP allowlisting.
- API key revocation management.
- Audit logging.
- Multiple API key environments (LIVE / TEST).
- Key usage monitoring and alerts.

## 🎯 Learning Objectives

This project demonstrates practical implementation of:

1. Custom authentication filters in Spring Security.
2. Secure credential storage and verification.
3. Role and scope-based authorization.
4. Stateless security architecture.
5. Request correlation and distributed tracing fundamentals.
6. Centralized REST API exception handling.

---

**Project:** Secure M2M API Gateway  
**Category:** Backend Engineering | API Security | Authentication & Authorization
