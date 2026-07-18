# 📖 QR Generator — Concepts & API Guide

A hands-on companion to the README. This file does two things:

1. **Explains the new concepts** this project introduces — in plain English, with the "why".
2. **Walks through every API** with a copy-paste example, the exact response you get, and where you'd actually use it.

> **Base URL for everything below:** `http://localhost:8080/api`
> (Spring's `context-path` is `/api`, and the controller is mapped to `/qrcodes`.)
>
> Start the app first: `docker compose up --build` **or** `./mvnw spring-boot:run`,
> then open Swagger UI at `http://localhost:8080/api/swagger-ui.html`.

---

## 🧠 Part 1 — New Concepts Explained

These are the ideas worth understanding before you fire the requests. Each one shows up in the API examples in Part 2.

### 1. A QR code is just a string — the *format* triggers the phone

A QR code has **no "type" field**. It only stores text. The magic is a **convention**: scanner apps (iOS Camera, Google Lens) look at the *shape* of that text and decide what to do.

| If the text looks like…            | The phone offers to…      |
|------------------------------------|---------------------------|
| `https://example.com`              | Open the link             |
| `WIFI:T:WPA;S:Cafe;P:pass;;`       | Join the Wi-Fi            |
| `BEGIN:VCARD…END:VCARD`            | Add a contact             |
| `mailto:you@x.com`                 | Draft an email            |
| `tel:+91…`                         | Dial a number             |
| `geo:12.97,77.59`                  | Open maps                 |

So "generate a Wi-Fi QR" really means *"build the exact `WIFI:` string, then encode it"*. That string-building lives in `QrPayloadBuilder`.

### 2. Content classification (heuristic tagging)

Because the type isn't stored, we **detect** it by inspecting the payload prefix (`http://`, `wifi:`, `begin:vcard`, …). That's what `ContentType.classify()` does. This is what turns dumb history into useful **analytics**:

> Not *"we made 500 QR codes"* but *"62% URLs, 20% Wi-Fi, 18% contacts"*.

Possible values: `URL`, `EMAIL`, `PHONE`, `SMS`, `WIFI`, `GEO`, `VCARD`, `TEXT`.

### 3. Error correction levels (L / M / Q / H)

QR codes embed **Reed–Solomon redundancy** so a scratched or logo-covered code still scans. You choose how much:

| Level | Recovers ~ | Best for                                  |
|-------|-----------|--------------------------------------------|
| `L`   | 7%        | Clean screens, maximum data density        |
| `M`   | 15%       | **Sensible default**                       |
| `Q`   | 25%       | Printed media that gets scuffed / handled  |
| `H`   | 30%       | Codes with a logo in the middle            |

Trade-off: higher recovery ⇒ denser image ⇒ harder to scan from far away.

### 4. Two output formats — raw PNG vs Base64 **data URI**

The same image is served two ways because **clients differ**:

- **`POST /qrcodes/image`** → returns raw `image/png` bytes. Point an `<img src>` straight at it, or download it. Efficient and cacheable.
- **`POST /qrcodes`** → returns **JSON** where the image is a **data URI** string like `data:image/png;base64,iVBORw0KGgo…`. You can paste that straight into `<img src="…">` with **no second request**, and you also get metadata (the history `id`, detected type).

A data URI is the whole image encoded as text (Base64). Handy for SPAs, but ~33% larger — a deliberate trade-off, not a default for everything.

### 5. The engine is isolated on purpose (ports & adapters)

Only **one file** — `QrCodeEngine` — imports the ZXing library. Everything else (`QrCodeService`, controllers) talks to our own small interface. Benefits:

- Business logic is **unit-testable without the library**.
- Swapping the QR engine touches **one file**.
- ZXing's checked exceptions are translated into **our** domain exceptions at that single boundary.

### 6. Fail-fast validation (Bean Validation)

Every request DTO is annotated (`@NotBlank`, `@Size`, `@Pattern`, `@Min/@Max`, `@Email`). Validation runs **before** any encoding, so bad input never reaches the engine — it fails fast with a clear **400** and a per-field message.

### 7. One consistent error shape

*Every* failure — validation, bad upload, un-encodable content, unexpected — comes back in the **same JSON shape** (`ErrorResponse`), so clients write error handling once:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/qrcodes",
  "timestamp": "2026-07-18T10:15:30",
  "errors": { "foregroundColor": "foregroundColor must be a #RRGGBB hex value" }
}
```

| Situation                          | Status | When it happens                         |
|------------------------------------|--------|-----------------------------------------|
| Invalid field(s)                   | `400`  | Bad colour, blank content, etc.         |
| Unreadable image on decode         | `400`  | No QR found in the uploaded file        |
| Upload too big                     | `413`  | File exceeds the multipart limit        |
| Valid input but can't encode       | `422`  | Content too large to fit a QR           |
| Anything unexpected                | `500`  | Logged with a stack trace               |

### 8. A stable pagination wrapper

Spring Data's `Page` serializes to a big, unstable JSON blob. We map it to our own `PagedResponse` exposing only `content`, `page`, `size`, `totalElements`, `totalPages`, `last` — so the API contract stays stable even if Spring changes.

### 9. Metadata-only persistence

The database stores **only metadata** (content, type, size, timestamp) — **never the image bytes**. QR images are **deterministic**: the same content + options always produce the same PNG, so they can be regenerated for free. No BLOB bloat.

### 10. Cache-Control on deterministic images

Because a given input always yields the same image, the PNG endpoints send `Cache-Control: public, max-age=3600`, letting browsers/CDNs cache them safely.

---

## 🚀 Part 2 — Every API, with Examples

Below, each endpoint has: **what it does**, a **request** you can paste, the **response** you get back, and **where you'd use it**.

Endpoint summary:

| # | Method | Path                    | Returns            |
|---|--------|-------------------------|--------------------|
| 1 | POST   | `/qrcodes`              | JSON + data URI    |
| 2 | POST   | `/qrcodes/image`        | `image/png` bytes  |
| 3 | POST   | `/qrcodes/wifi`         | `image/png` bytes  |
| 4 | POST   | `/qrcodes/vcard`        | `image/png` bytes  |
| 5 | POST   | `/qrcodes/decode`       | JSON               |
| 6 | GET    | `/qrcodes/history`      | JSON (paginated)   |
| 7 | GET    | `/qrcodes/analytics`    | JSON               |
| 8 | GET    | `/actuator/health`      | JSON               |

---

### 1️⃣ Generate QR as JSON (embeddable data URI)

`POST /api/qrcodes`

**What it does:** encodes your text and returns JSON containing an embeddable Base64 data URI plus metadata and a history `id`.

**Request body fields** (only `content` is required; the rest default):

| Field             | Type   | Default     | Rules                                  |
|-------------------|--------|-------------|----------------------------------------|
| `content`         | string | —           | required, ≤ 2000 chars                 |
| `size`            | int    | `300`       | 50–2000 (px)                           |
| `margin`          | int    | `1`         | 0–50 (modules)                         |
| `foregroundColor` | string | `#000000`   | `#RRGGBB` hex                          |
| `backgroundColor` | string | `#FFFFFF`   | `#RRGGBB` hex                          |
| `errorCorrection` | enum   | `M`         | `L` / `M` / `Q` / `H`                  |

```bash
curl -X POST http://localhost:8080/api/qrcodes \
  -H "Content-Type: application/json" \
  -d '{"content":"https://github.com/backend-dockyard","size":300,"errorCorrection":"M"}'
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "content": "https://github.com/backend-dockyard",
  "contentType": "URL",
  "dataUri": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "size": 300,
  "byteSize": 612,
  "createdAt": "2026-07-18T10:15:30"
}
```

**Where you'd use it:** a React/Angular/Vue dashboard or mobile app that wants to **show the QR immediately** — drop `dataUri` into `<img src="…">` with no extra request — and also needs the metadata (`id`, detected `contentType`) in the same call.

---

### 2️⃣ Generate QR as a PNG image

`POST /api/qrcodes/image`

**What it does:** same generation, but returns **raw `image/png` bytes** instead of JSON. Same request body as endpoint #1.

```bash
curl -X POST http://localhost:8080/api/qrcodes/image \
  -H "Content-Type: application/json" \
  -d '{"content":"Round trip works!","foregroundColor":"#1A237E","backgroundColor":"#E8EAF6"}' \
  --output qr.png
open qr.png     # macOS
```

**Response `200 OK`:** binary PNG (with `Content-Type: image/png` and `Cache-Control: public, max-age=3600`). Saved here as `qr.png`.

**Where you'd use it:** anywhere that consumes an image URL directly — an HTML `<img>`, a "download QR" button, a printable poster, or a server-side PDF/ticket generator.

---

### 3️⃣ Generate a Wi-Fi join QR

`POST /api/qrcodes/wifi`

**What it does:** you send **friendly fields**, the service builds the exact `WIFI:T:…;S:…;P:…;;` string (with correct escaping) and returns a PNG. Scanning it joins the network with **no typing**.

**Request body fields:**

| Field             | Type    | Default | Rules                        |
|-------------------|---------|---------|------------------------------|
| `ssid`            | string  | —       | required, ≤ 100 chars        |
| `password`        | string  | —       | ≤ 100 chars (omit for open)  |
| `encryption`      | string  | `WPA`   | `WPA` / `WEP` / `nopass`     |
| `hidden`          | boolean | `false` | true if SSID isn't broadcast |
| `size`            | int     | `300`   | 50–2000 px                   |
| `errorCorrection` | enum    | `M`     | `L`/`M`/`Q`/`H`              |

```bash
curl -X POST http://localhost:8080/api/qrcodes/wifi \
  -H "Content-Type: application/json" \
  -d '{"ssid":"Cafe-Guest","password":"latte123","encryption":"WPA"}' \
  --output wifi.png
```

**Response `200 OK`:** a PNG. Encoded payload = `WIFI:T:WPA;S:Cafe-Guest;P:latte123;;`
*(Decode it with endpoint #5 to see that exact string.)*

**Where you'd use it:** café/hotel/office guest Wi-Fi, printed on a table tent or reception card; event/venue networks; a "share my Wi-Fi" feature in an app.

---

### 4️⃣ Generate a contact-card (vCard) QR

`POST /api/qrcodes/vcard`

**What it does:** encodes a contact as a **vCard 3.0** payload. Scanning opens the phone's pre-filled **"add contact"** screen. Only non-empty fields are included. Defaults to `errorCorrection = Q` (contact cards carry more data).

**Request body fields:**

| Field             | Type   | Rules                       |
|-------------------|--------|-----------------------------|
| `fullName`        | string | required, ≤ 100 chars       |
| `phone`           | string | ≤ 30 chars                  |
| `email`           | string | valid email                 |
| `organization`    | string | ≤ 100 chars                 |
| `title`           | string | ≤ 100 chars                 |
| `website`         | string | ≤ 200 chars                 |
| `size`            | int    | 50–2000 px (default 300)    |
| `errorCorrection` | enum   | `L`/`M`/`Q`/`H` (default Q) |

```bash
curl -X POST http://localhost:8080/api/qrcodes/vcard \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","phone":"+911234567890","email":"ada@x.io","organization":"Analytical Engines"}' \
  --output contact.png
```

**Response `200 OK`:** a PNG. Encoded payload:
```
BEGIN:VCARD
VERSION:3.0
N:Ada Lovelace
FN:Ada Lovelace
ORG:Analytical Engines
TEL;TYPE=CELL:+911234567890
EMAIL:ada@x.io
END:VCARD
```

**Where you'd use it:** business cards, conference badges, e-mail signatures, "add me" on a profile page.

---

### 5️⃣ Decode a QR image back to text (reverse direction)

`POST /api/qrcodes/decode` — `multipart/form-data`

**What it does:** you upload a PNG/JPG containing a QR code; it reads the code and returns the original text plus its classification.

```bash
curl -X POST http://localhost:8080/api/qrcodes/decode -F "file=@qr.png"
```

**Response `200 OK`:**
```json
{
  "content": "Round trip works!",
  "contentType": "TEXT",
  "format": "QR_CODE"
}
```

Decode the Wi-Fi image from #3 and you'd see:
```json
{ "content": "WIFI:T:WPA;S:Cafe-Guest;P:latte123;;", "contentType": "WIFI", "format": "QR_CODE" }
```

**Error `400`** if the image has no readable QR:
```json
{ "status": 400, "error": "Decode Failed", "message": "No QR code found in the image", "path": "/api/qrcodes/decode", "timestamp": "…" }
```

**Where you'd use it:** a scanner/upload feature, verifying a QR you generated (round-trip test), or ingesting screenshots users paste in.

---

### 6️⃣ Generation history (paginated)

`GET /api/qrcodes/history?page=0&size=10`

**What it does:** lists previously generated codes, **newest first**, wrapped in the stable pagination envelope.

| Query param | Default | Meaning               |
|-------------|---------|-----------------------|
| `page`      | `0`     | zero-based page index |
| `size`      | `10`    | items per page        |

```bash
curl "http://localhost:8080/api/qrcodes/history?page=0&size=10"
```

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 2,
      "content": "Round trip works!",
      "contentType": "TEXT",
      "sizePx": 300,
      "errorCorrection": "M",
      "byteSize": 540,
      "createdAt": "2026-07-18T10:16:05"
    },
    {
      "id": 1,
      "content": "https://github.com/backend-dockyard",
      "contentType": "URL",
      "sizePx": 300,
      "errorCorrection": "M",
      "byteSize": 612,
      "createdAt": "2026-07-18T10:15:30"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

**Where you'd use it:** an admin/history screen, an audit trail, or "your recent QR codes" in a user account.

---

### 7️⃣ Usage analytics

`GET /api/qrcodes/analytics`

**What it does:** aggregates the whole history into dashboard-ready numbers using a single grouped SQL query.

```bash
curl "http://localhost:8080/api/qrcodes/analytics"
```

**Response `200 OK`:**
```json
{
  "totalGenerated": 5,
  "byContentType": { "URL": 3, "WIFI": 1, "TEXT": 1 },
  "mostCommonType": "URL"
}
```

**Where you'd use it:** a metrics dashboard, product analytics ("what are people generating?"), or capacity/usage reporting.

---

### 8️⃣ Health check

`GET /api/actuator/health`

**What it does:** Spring Boot Actuator liveness/readiness probe.

```bash
curl "http://localhost:8080/api/actuator/health"
```

**Response `200 OK`:**
```json
{ "status": "UP" }
```

**Where you'd use it:** Kubernetes liveness/readiness probes, load-balancer health checks, uptime monitoring.

---

## 🧪 Bonus — See validation & errors in action

```bash
curl -X POST http://localhost:8080/api/qrcodes \
  -H "Content-Type: application/json" \
  -d '{"content":"x","foregroundColor":"red"}'
```
**Response `400`:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "path": "/api/qrcodes",
  "timestamp": "2026-07-18T10:20:00",
  "errors": { "foregroundColor": "foregroundColor must be a #RRGGBB hex value" }
}
```

---

## 🔁 End-to-end round trip (copy-paste)

Proves generate → decode returns the same text:

```bash
# 1. Generate a PNG
curl -s -X POST http://localhost:8080/api/qrcodes/image \
  -H "Content-Type: application/json" \
  -d '{"content":"https://backend-dockyard.dev"}' --output rt.png

# 2. Decode it back
curl -s -X POST http://localhost:8080/api/qrcodes/decode -F "file=@rt.png"
# → {"content":"https://backend-dockyard.dev","contentType":"URL","format":"QR_CODE"}
```

---

## 📚 Where to go next

- **Swagger UI** (try everything interactively): `http://localhost:8080/api/swagger-ui.html`
- **H2 console** (inspect the `qr_codes` table): `http://localhost:8080/api/h2-console`
  (JDBC URL `jdbc:h2:mem:qrdb`, user `sa`, blank password)
- **README.md** — architecture, design rationale, and interview Q&A.