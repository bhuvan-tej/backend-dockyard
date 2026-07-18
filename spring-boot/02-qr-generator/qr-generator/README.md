# 🔳 QR Generator

## 🎯 Goal

---
Build a **production-shaped QR code service** that goes beyond "text in, image out".
It generates, styles, decodes and *analyses* QR codes — and ships with real-world
helper endpoints (WiFi join, contact cards) that show **how QR codes are actually used**.

The point isn't the QR library. It's demonstrating a clean, layered Spring Boot
design where a third-party engine is fully isolated, every input is validated,
every error is consistent, and the whole thing is genuinely runnable with
**zero external setup** (in-memory H2).

## 💡 What Can a QR Code Actually Be Used For?

---
A QR code is just a way to hand a string to a phone camera. The *magic* is that
scanners recognise conventional string formats and take action automatically:

| Use case           | Payload the QR encodes        | What the phone does            |
|--------------------|-------------------------------|--------------------------------|
| **Website / link** | `https://example.com`         | Opens the URL                  |
| **WiFi sharing**   | `WIFI:T:WPA;S:Cafe;P:pass;;`  | Joins the network, no typing   |
| **Contact card**   | `BEGIN:VCARD…END:VCARD`       | Opens pre-filled "add contact" |
| **Payment (UPI)**  | `upi://pay?pa=me@bank&am=100` | Opens the payment app          |
| **Email**          | `mailto:me@x.com`             | Drafts an email                |
| **Phone / SMS**    | `tel:+91…` / `smsto:+91…:hi`  | Dials / drafts a text          |
| **Location**       | `geo:12.97,77.59`             | Opens maps                     |
| **App deep link**  | `myapp://screen/42`           | Jumps into a mobile app screen |

This project ships first-class helpers for **URL, WiFi and vCard**, classifies
every code by type, and reports analytics like *"62% of codes generated were URLs"*.

## 🏗️ Architecture

---
```
HTTP Request
      │
      ▼
QrCodeController        validates (@Valid), picks PNG vs JSON output
      │                 maps bytes → data URI, sets content types
      ▼
QrCodeService           business logic: classify, generate, record, aggregate
      │                 NEVER imports ZXing or HTTP types
      ├──────────────► QrPayloadBuilder   builds WIFI:/vCard payload strings
      ├──────────────► QrCodeEngine        the ONLY class that touches ZXing
      │                                    encode() + decode(), error translation
      ▼
QrCodeRecordRepository  Spring Data JPA — history + analytics aggregate query
      │
      ▼
H2 (in-memory)          qr_codes table (metadata only — never the image bytes)
```

**The key design decision:** every ZXing type lives in exactly one file
(`QrCodeEngine`). Swapping the QR library or unit-testing business logic never
touches the rest of the app. This is the "hexagonal / ports-and-adapters" idea
applied at small scale.

## 📁 Project Structure

---
```
qr-generator/
├── src/main/java/com/dockyard/qrgenerator/
│   ├── QrGeneratorApplication.java        entry point
│   ├── config/
│   │   └── OpenApiConfig.java             Swagger UI configuration
│   ├── controller/
│   │   └── QrCodeController.java          all HTTP endpoints (thin)
│   ├── domain/
│   │   ├── ContentType.java              heuristic classification (URL/WIFI/…)
│   │   └── ErrorCorrection.java          L/M/Q/H, decoupled from ZXing
│   ├── dto/
│   │   ├── QrCodeRequest.java            generic generate request + validation
│   │   ├── WifiQrRequest.java            friendly WiFi fields
│   │   ├── VCardQrRequest.java           friendly contact fields
│   │   ├── QrCodeResponse.java           JSON response with Base64 data URI
│   │   ├── QrDecodeResponse.java         decode result
│   │   ├── QrHistoryResponse.java        one history row
│   │   ├── QrAnalyticsResponse.java      aggregated stats
│   │   └── PagedResponse.java            generic pagination wrapper
│   ├── engine/
│   │   ├── QrCodeEngine.java             ★ the only ZXing consumer
│   │   └── QrPayloadBuilder.java         WiFi/vCard grammar + escaping
│   ├── entity/
│   │   └── QrCodeRecord.java             qr_codes table
│   ├── exception/
│   │   ├── ErrorResponse.java            consistent error shape
│   │   ├── QrGenerationException.java    422 — content won't fit
│   │   ├── QrDecodingException.java      400 — unreadable image
│   │   └── GlobalExceptionHandler.java   catches everything
│   ├── repository/
│   │   └── QrCodeRecordRepository.java   history + analytics query
│   └── service/
│       ├── QrCodeService.java            business logic
│       └── GeneratedQr.java              internal (png bytes + record) carrier
├── src/main/resources/
│   └── application.yml
├── src/test/java/com/dockyard/qrgenerator/
│   ├── engine/QrCodeEngineTest.java      ★ real encode→decode round-trip
│   ├── engine/QrPayloadBuilderTest.java  WiFi/vCard grammar + escaping
│   ├── domain/ContentTypeTest.java       parameterised classification
│   ├── controller/QrCodeControllerTest.java  MockMvc, mocked service
│   └── QrGeneratorApplicationTests.java  context smoke test
├── Dockerfile                            multi-stage build → slim JRE
├── docker-compose.yml                    one-command run
├── .dockerignore
└── pom.xml
```

## 🔑 Key Concepts

---
### Error Correction (L / M / Q / H)
```
QR codes use Reed-Solomon redundancy so a damaged code still scans.
  L  ~7%   maximum data, clean screens
  M  ~15%  the sensible default
  Q  ~25%  printed media that gets scuffed
  H  ~30%  codes with a logo in the middle
Higher recovery = denser image = harder to scan from far away. Trade-offs.
```

### Two output formats — because clients differ
```
POST /api/qrcodes/image  → raw image/png     (point an <img src> at it / download)
POST /api/qrcodes        → JSON + data URI    (embed in a SPA, get the history id too)
```

### The engine is isolated on purpose
```
Only QrCodeEngine imports com.google.zxing.*
  → business logic is testable without the library
  → swapping engines touches one file
  → ZXing's checked exceptions become our domain exceptions at the boundary
```

### Consistent errors
```
MethodArgumentNotValidException → 400  { errors: { field: reason } }
QrDecodingException             → 400  unreadable image
MaxUploadSizeExceededException  → 413  upload too big
QrGenerationException           → 422  valid input, impossible to encode
Exception (catch-all)           → 500  logged with stack trace
Every failure has the same ErrorResponse shape.
```

## ✅ Running Locally

> **No database to install.** The app uses in-memory H2 — just Java 21+.

### Option A — Maven wrapper
```bash
./mvnw spring-boot:run
```

### Option B — build a jar and run it
```bash
./mvnw clean package
java -jar target/qr-generator-1.0.0.jar
```

### Option C — Docker (one command, no Java needed)
```bash
docker compose up --build
```

Then open **Swagger UI** and try every endpoint from the browser:
```
http://localhost:8080/api/swagger-ui.html
```

Inspect the in-memory data (JDBC URL `jdbc:h2:mem:qrdb`, user `sa`, no password):
```
http://localhost:8080/api/h2-console
```

> 📖 **New here?** Read **[API_GUIDE.md](API_GUIDE.md)** — it explains every concept
> in plain English and walks through **all 8 endpoints** with copy-paste requests,
> the exact responses you get back, and where each one is used in the real world.

## 🧪 Testing It Out

### 1. Generate a QR as JSON (embeddable data URI)
```bash
curl -X POST http://localhost:8080/api/qrcodes \
  -H "Content-Type: application/json" \
  -d '{"content":"https://github.com/backend-dockyard","size":300,"errorCorrection":"M"}'
```
Response:
```json
{
  "id": 1,
  "content": "https://github.com/backend-dockyard",
  "contentType": "URL",
  "dataUri": "data:image/png;base64,iVBORw0KGgo...",
  "size": 300,
  "byteSize": 612,
  "createdAt": "2026-07-17T17:38:00"
}
```
Paste the `dataUri` straight into an HTML `<img src="...">` — no extra request.

### 2. Generate a PNG image and open it
```bash
curl -X POST http://localhost:8080/api/qrcodes/image \
  -H "Content-Type: application/json" \
  -d '{"content":"Round trip works!","foregroundColor":"#1A237E","backgroundColor":"#E8EAF6"}' \
  --output qr.png
open qr.png     # macOS
```

### 3. Decode a QR image back to text (the reverse direction)
```bash
curl -X POST http://localhost:8080/api/qrcodes/decode -F "file=@qr.png"
# → {"content":"Round trip works!","contentType":"TEXT","format":"QR_CODE"}
```

### 4. WiFi join code — scan it to connect with no typing
```bash
curl -X POST http://localhost:8080/api/qrcodes/wifi \
  -H "Content-Type: application/json" \
  -d '{"ssid":"Cafe-Guest","password":"latte123","encryption":"WPA"}' \
  --output wifi.png
```

### 5. Contact card (vCard) code
```bash
curl -X POST http://localhost:8080/api/qrcodes/vcard \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","phone":"+911234567890","email":"ada@x.io","organization":"Analytical Engines"}' \
  --output contact.png
```

### 6. Validation in action
```bash
curl -X POST http://localhost:8080/api/qrcodes \
  -H "Content-Type: application/json" \
  -d '{"content":"x","foregroundColor":"red"}'
# → 400 {"errors":{"foregroundColor":"foregroundColor must be a #RRGGBB hex value"}}
```

### 7. History and analytics
```bash
curl "http://localhost:8080/api/qrcodes/history?page=0&size=10"
curl "http://localhost:8080/api/qrcodes/analytics"
# → {"totalGenerated":5,"byContentType":{"URL":3,"WIFI":1,"TEXT":1},"mostCommonType":"URL"}
```

## 📋 Endpoints

---
| Method | URL                        | Description                          | Success | Errors        |
|--------|----------------------------|--------------------------------------|---------|---------------|
| POST   | /api/qrcodes/image         | Generate QR → PNG image              | 200     | 400 / 422     |
| POST   | /api/qrcodes               | Generate QR → JSON (Base64 data URI) | 200     | 400 / 422     |
| POST   | /api/qrcodes/wifi          | WiFi-join QR (PNG)                   | 200     | 400 / 422     |
| POST   | /api/qrcodes/vcard         | Contact-card QR (PNG)                | 200     | 400 / 422     |
| POST   | /api/qrcodes/decode        | Decode uploaded image → text         | 200     | 400 / 413     |
| GET    | /api/qrcodes/history       | Paginated generation history         | 200     | —             |
| GET    | /api/qrcodes/analytics     | Usage breakdown by content type      | 200     | —             |
| GET    | /api/actuator/health       | Health check                         | 200     | —             |

## 🧰 Tech Stack

---
| Tool                        | Purpose                              |
|-----------------------------|--------------------------------------|
| Java 21 (LTS)               | Language                             |
| Spring Boot 3.5             | Web, Validation, Data JPA, Actuator  |
| ZXing 3.5.3                 | QR encode / decode engine            |
| H2 (in-memory)              | Zero-setup history storage           |
| SpringDoc OpenAPI           | Swagger UI                           |
| Lombok                      | Boilerplate reduction                |
| JUnit 5 + Mockito + MockMvc | Testing (24 tests, incl. round-trip) |
| Docker (multi-stage)        | Slim, non-root runtime image         |

## 💡 Interview Questions

---
**Q: Why isolate the ZXing library behind a single `QrCodeEngine` class?**
> It's the adapter pattern / dependency inversion. The service depends on a
narrow interface we own, not on ZXing's API. That makes business logic testable
without the library, confines third-party checked exceptions to one boundary
where we translate them into our own domain exceptions, and means swapping the
QR engine changes exactly one file instead of rippling through the codebase.

**Q: You return the image two ways — raw PNG and a Base64 data URI. Why?**
> Different clients want different things. A browser or a simple page can point
an `<img src>` directly at the raw `image/png` endpoint — that's efficient and
cacheable. A single-page app or mobile client often wants the image *and*
metadata (the history id, detected type) in one JSON response with no second
request; a Base64 data URI embeds cleanly there. Base64 adds ~33% size, so it's
a deliberate trade-off, not a default for everything.

**Q: Why store only metadata in the database, not the QR image bytes?**
> QR images are deterministic — the same content plus options always produce the
same PNG, so they can be regenerated for free at any time. Storing bytes would
bloat the database and add BLOB-handling complexity for zero benefit. We store
what analytics actually needs: content, type, size, timestamp.

**Q: What is QR error correction and why expose it?**
> QR codes embed Reed-Solomon redundancy so a torn, dirty or logo-covered code
still decodes. Levels L/M/Q/H recover ~7/15/25/30%. Higher levels survive more
damage but make the image denser and harder to scan from a distance — a real
trade-off callers should control based on where the code will live (a clean
screen vs a printed poster with a logo).

**Q: How does the WiFi QR code actually work?**
> It's not magic — it's a conventional string format:
`WIFI:T:<auth>;S:<ssid>;P:<password>;H:<hidden>;;`. Phone cameras recognise the
`WIFI:` prefix and offer to join. The hard part is escaping: characters like
`;`, `:`, `,` and `\` inside an SSID or password must be backslash-escaped or the
grammar breaks silently. `QrPayloadBuilder` handles that, and there's a unit test
pinning the escaping down.

**Q: How would you make history persist across restarts / scale horizontally?**
> Swap the H2 datasource block in `application.yml` for PostgreSQL — nothing else
changes because JPA abstracts the database. For scale, the app is already
stateless (no session state), so you run multiple instances behind a load
balancer pointing at the shared Postgres. That's the natural next step and mirrors
the containerised PostgreSQL setup used elsewhere in this repo.

