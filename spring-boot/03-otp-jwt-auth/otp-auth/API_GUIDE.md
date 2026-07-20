# 📖 OTP + JWT Auth — Concepts & API Guide

A hands-on companion to the README. This file does two things:

1. **Explains the concepts** this project introduces — in plain English, with the "why".
2. **Walks through every API** with a copy-paste example, the exact response you get, and where you'd actually use it.

> **Base URL for everything below:** `http://localhost:8080/api`
> (Spring's `context-path` is `/api`; the controllers are mapped to `/otp` and `/auth`.)
>
> Start the app first: `docker compose up --build` **or** `./mvnw spring-boot:run`,
> then open Swagger UI at `http://localhost:8080/api/swagger-ui.html`.

---

## 🧠 Part 1 — Concepts Explained

### 1. What an OTP actually is

An OTP (one-time password) is a **short, random, short-lived secret** proving you
control an identifier (email/phone). The server generates it, "sends" it to you,
and you send it back. If it matches — and hasn't expired or been used — you're in.

This app generates a **6-digit** code with `SecureRandom` (a cryptographically
strong generator, *not* `java.util.Random`, which is predictable).

### 2. The OTP is hashed — never stored in clear

The database stores a **BCrypt hash** of the code, not the code itself (exactly
how passwords should be stored). If the DB leaked, the codes couldn't be read
back. On verify, the submitted code is hashed and compared.

### 3. Validity & expiry — the whole theme

Three secrets, three independent clocks, all enforced server-side:

| Secret            | Default life | How expiry is enforced                           |
|-------------------|--------------|--------------------------------------------------|
| **OTP**           | 5 min        | `expiresAt` column checked before issuing tokens |
| **Access token**  | 15 min       | signed JWT `exp` claim, checked on every request |
| **Refresh token** | 7 days       | signed JWT `exp` claim, checked on refresh       |

Because the JWT `exp` is **signed**, a client can't extend its own token —
editing it breaks the signature and the request is rejected with 401.

### 4. The verify gauntlet (order matters)

`POST /otp/verify` runs these checks in order:

```
1. challenge exists?  → 404 if the challengeId is unknown
2. already used?      → 401 (an OTP works exactly once)
3. expired?           → 410 (validity window elapsed)
4. locked out?        → 429 (too many wrong attempts)
5. code matches?      → 401 + attemptsRemaining, else success
```

### 5. Two tokens, two jobs

- **Access token** — sent on *every* request as `Authorization: Bearer <token>`.
  Short-lived, so a leak is only dangerous for minutes.
- **Refresh token** — sent *only* to `/auth/refresh` to mint a new access token.
  Long-lived, so you're not forced to re-login constantly.

A `type` claim (`ACCESS` / `REFRESH`) is baked into each token and checked on the
way in, so a refresh token can never be used to access a protected endpoint.

### 6. Stateless security (no sessions)

There is no server-side session and no CSRF token. Every request proves itself
with its Bearer token. A single filter (`JwtAuthenticationFilter`) validates the
token and populates Spring Security's context; `SecurityConfig` decides which
paths are public and which need a token. This makes horizontal scaling trivial.

### 7. The JWT engine is isolated (ports & adapters)

Only **one file** — `JwtService` — imports the JJWT library. Everything else uses
our own `ParsedToken`. Swapping the JWT library, or moving to opaque tokens,
touches that one file. (Same principle as the QR project isolating ZXing.)

### 8. One consistent error shape

Every failure — validation, expired OTP, bad token, even the security 401 — comes
back in the same JSON:

```json
{
  "status": 401,
  "error": "Invalid OTP",
  "message": "Incorrect code",
  "path": "/api/otp/verify",
  "timestamp": "2026-07-18T10:55:48",
  "errors": { "attemptsRemaining": "4" }
}
```

| Situation                      | Status | When it happens                           |
|--------------------------------|--------|-------------------------------------------|
| Invalid field(s)               | `400`  | Missing identifier, malformed code        |
| Wrong / already-used OTP       | `401`  | Bad code, or an OTP reused                |
| Bad / expired / wrong-type JWT | `401`  | Refresh with a junk or expired token      |
| Unknown challengeId            | `404`  | Verifying against a challenge that's gone |
| Expired OTP                    | `410`  | Code correct-shaped but past its window   |
| Locked out / resend too soon   | `429`  | Attempt limit hit, or spamming requests   |
| Anything unexpected            | `500`  | Logged with a stack trace                 |

### 9. Dev-mode OTP echo (zero-setup testing)

Real systems text/email the code. To keep this runnable with **no gateway**, when
`otp.expose-code=true` (the default) the code is returned in the response as
`devCode` and logged. Set it to `false` (or `OTP_EXPOSE_CODE=false`) to harden.

---

## 🚀 Part 2 — Every API, with Examples

Endpoint summary:

| # | Method | Path               | Auth       | Returns                 |
|---|--------|--------------------|------------|-------------------------|
| 1 | POST   | `/otp/request`     | public     | challenge + devCode     |
| 2 | POST   | `/otp/verify`      | public     | access + refresh tokens |
| 3 | GET    | `/auth/me`         | **Bearer** | identity + token expiry |
| 4 | POST   | `/auth/refresh`    | public     | new tokens              |
| 5 | GET    | `/actuator/health` | public     | health                  |

---

### 1️⃣ Request an OTP

`POST /api/otp/request`

**What it does:** generates a short-lived OTP for the identifier, stores its hash
with an expiry, and (dev mode) returns the code.

**Request body fields** (only `identifier` is required):

| Field        | Type   | Default | Rules                                           |
|--------------|--------|---------|-------------------------------------------------|
| `identifier` | string | —       | required, ≤ 320 chars                           |
| `purpose`    | enum   | `LOGIN` | `LOGIN`/`SIGNUP`/`PASSWORD_RESET`/`TRANSACTION` |
| `channel`    | enum   | `EMAIL` | `EMAIL`/`SMS`                                   |

```bash
curl -X POST http://localhost:8080/api/otp/request \
  -H "Content-Type: application/json" \
  -d '{"identifier":"ada@dockyard.dev","purpose":"LOGIN","channel":"EMAIL"}'
```

**Response `200 OK`:**
```json
{
  "challengeId": "1e358091-1756-4edf-a94c-f07a8c31fea2",
  "identifier": "ada@dockyard.dev",
  "purpose": "LOGIN",
  "channel": "EMAIL",
  "expiresAt": "2026-07-18T11:00:46",
  "ttlSeconds": 300,
  "maxAttempts": 5,
  "resendAfterSeconds": 30,
  "message": "OTP generated for a***@dockyard.dev",
  "devCode": "400176"
}
```

**Error `429`** if you request again within the resend cooldown:
```json
{ "status": 429, "error": "Too Many Requests", "message": "An OTP was just requested — please wait before requesting another", "path": "/api/otp/request", "timestamp": "…" }
```

**Where you'd use it:** the "send me a code" button on a login/signup screen, or
a step-up check before a sensitive action (`purpose: TRANSACTION`).

---

### 2️⃣ Verify the OTP → get JWT tokens

`POST /api/otp/verify`

**What it does:** checks the code against the challenge and, on success, consumes
the OTP (single-use) and returns an access + refresh token pair.

**Request body fields:**

| Field         | Type   | Rules                        |
|---------------|--------|------------------------------|
| `challengeId` | string | required                     |
| `code`        | string | required, 4–10 digits        |

```bash
curl -X POST http://localhost:8080/api/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"challengeId":"1e358091-1756-4edf-a94c-f07a8c31fea2","code":"400176"}'
```

**Response `200 OK`:**
```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJkb2NreWFyZC1vdHAtYXV0aCIsInN1YiI6ImFkYUBkb2NreWFyZC5kZXYiLCJ0eXBlIjoiQUNDRVNTIiwiaWF0IjoxNzg0MzUyMzQ3LCJleHAiOjE3ODQzNTMyNDd9.ari1s5__HYC1Jkcc...",
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJkb2NreWFyZC1vdHAtYXV0aCIsInN1YiI6ImFkYUBkb2NreWFyZC5kZXYiLCJ0eXBlIjoiUkVGUkVTSCIsImlhdCI6...",
  "accessTokenExpiresInSeconds": 900,
  "refreshTokenExpiresInSeconds": 604800,
  "subject": "ada@dockyard.dev"
}
```

**Common errors:**
```
// wrong code — 401, tells you how many tries remain
{ "status": 401, "error": "Invalid OTP", "message": "Incorrect code", "errors": { "attemptsRemaining": "4" }, "path": "/api/otp/verify", "timestamp": "…" }

// reused code — 401
{ "status": 401, "error": "Invalid OTP", "message": "This OTP has already been used", "errors": { "attemptsRemaining": "0" }, "…": "…" }

// expired — 410
{ "status": 410, "error": "OTP Expired", "message": "This OTP has expired — request a new one", "…": "…" }

// unknown challengeId — 404
{ "status": 404, "error": "Not Found", "message": "No OTP challenge found for the supplied challengeId", "…": "…" }
```

**Where you'd use it:** the "verify code" step that logs the user in. The returned
access token is what your frontend stores and sends on subsequent calls.

> 💡 **Tip:** paste `accessToken` into Swagger's **Authorize** button to call the
> protected `/auth/me` endpoint straight from the browser.

---

### 3️⃣ Who am I? (protected)

`GET /api/auth/me` — requires `Authorization: Bearer <accessToken>`

**What it does:** returns the identity the server trusts for the current access
token, including exactly when it expires — handy for refreshing proactively.

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

**Response `200 OK`:**
```json
{
  "subject": "ada@dockyard.dev",
  "issuedAt": "2026-07-18T05:25:47Z",
  "expiresAt": "2026-07-18T05:40:47Z",
  "expiresInSeconds": 900,
  "authorities": ["ROLE_USER"]
}
```

**Error `401`** with no/invalid/expired token (note: same JSON shape as everything else):
```json
{ "status": 401, "error": "Unauthorized", "message": "A valid Bearer access token is required to access this resource", "path": "/api/auth/me", "timestamp": "…", "errors": null }
```

**Where you'd use it:** any protected resource. `/auth/me` stands in for "your
profile", "your dashboard data", etc. — the pattern is identical: the filter
validates the token before your controller ever runs.

---

### 4️⃣ Refresh the access token

`POST /api/auth/refresh`

**What it does:** exchanges a **valid, unexpired refresh token** for a brand-new
access + refresh pair — so the user stays logged in without re-entering an OTP.

> 🔄 **Rotation:** a refresh token works **exactly once**. Using it revokes the
> old token and returns a new one. Replaying an already-used refresh token is
> treated as theft and revokes **every** refresh token for that user (they must
> log in again). Store the *new* `refreshToken` from each response and discard the old.

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

**Response `200 OK`:** same shape as `/otp/verify` (a fresh `TokenResponse`).

**Error `401`** if the refresh token is expired, tampered, an access token, or **already used**:
```json
{ "status": 401, "error": "Invalid Token", "message": "Token has expired", "path": "/api/auth/refresh", "timestamp": "…" }
```
```json
{ "status": 401, "error": "Invalid Token", "message": "Refresh token has already been used - all sessions revoked", "path": "/api/auth/refresh", "timestamp": "…" }
```

**Where you'd use it:** silently, in the background, when the access token is about
to expire (your client watches `expiresInSeconds`) — the user never notices.

---

### 5️⃣ Health check

`GET /api/actuator/health`

```bash
curl http://localhost:8080/api/actuator/health
# → {"status":"UP", ...}
```

**Where you'd use it:** Kubernetes probes, load-balancer checks, uptime monitors.

---

## 📚 Where to go next

- **Swagger UI** (try everything, incl. the Authorize button): `http://localhost:8080/api/swagger-ui.html`
- **H2 console** (inspect the `otp_challenges` table): `http://localhost:8080/api/h2-console`
  (JDBC URL `jdbc:h2:mem:otpdb`, user `sa`, blank password)
- **README.md** — architecture, design rationale, and interview Q&A.