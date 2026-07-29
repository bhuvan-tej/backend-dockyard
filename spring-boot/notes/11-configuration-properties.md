# 11 · Configuration & Properties

> Externalise settings so the same JAR runs in dev, test, and prod without recompiling.

---

## 📄 `application.yml` (or `.properties`)

Lives in `src/main/resources`. Holds config values:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dockyard
    username: ${DB_USER:postgres}      # env var with a default
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

rag:                                    # our custom group
  chunk-size: 800
  chunk-overlap: 100
  top-k: 4
```

- `${ENV_VAR:default}` — read from environment, fall back to a default.
- YAML is hierarchical and cleaner than `.properties` for nested config.

---

## 🎯 Two ways to read config

### 1. `@Value` — a single value

```java
@Component
public class Banner {
    @Value("${server.port}")           // inject one property
    private int port;

    @Value("${app.name:Dockyard}")     // with a default
    private String appName;
}
```
Good for one-off values. Verbose and untyped for groups.

### 2. `@ConfigurationProperties` — a typed group (preferred)

Binds a whole prefix to a class:

```java
@ConfigurationProperties(prefix = "rag")   // binds all rag.* properties
public class RagProperties {
    private int chunkSize;                  // ← rag.chunk-size (relaxed binding)
    private int chunkOverlap;               // ← rag.chunk-overlap
    private int topK;                       // ← rag.top-k
    // getters/setters (or use a record / @Data)
}
```

**Relaxed binding**: `chunk-size` (yaml) ↔ `chunkSize` (Java) ↔ `CHUNK_SIZE` (env) all match.

---

## 🔌 Activating `@ConfigurationProperties`

The properties class must become a **bean**. Two options:

```java
// Option A — register from a config class
@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig { }

// Option B — annotate the properties class itself
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties { ... }
```
Then inject it anywhere:
```java
@Service
@RequiredArgsConstructor
public class RagService {
    private final RagProperties props;      // typed, autocompleted config
}
```

### `@Value` vs `@ConfigurationProperties`

| | `@Value` | `@ConfigurationProperties` |
|---|----------|-----------------------------|
| Scope | single value | group of related values |
| Type safety | string-ish | strongly typed class |
| Relaxed binding | ❌ | ✅ |
| Validation (`@Validated`) | ❌ | ✅ |
| Best for | one-off | feature config (this repo uses it for `rag`, `jwt`, `otp`) |

---

## 🌱 Profiles — per-environment config

Create profile-specific files and activate one:

```
application.yml          # common defaults
application-dev.yml      # dev overrides
application-prod.yml     # prod overrides
```

```yaml
# activate a profile
spring:
  profiles:
    active: dev
```
Or via env/CLI: `SPRING_PROFILES_ACTIVE=prod` / `--spring.profiles.active=prod`.

Mark beans/config for a profile:
```java
@Profile("dev")
@Bean CommandLineRunner seedData(...) { ... }   // only runs in dev
```

---

## 🔐 Keeping secrets out of source

- Use **environment variables** (`${DB_PASSWORD}`) — never hardcode secrets.
- In containers, inject via env or mounted secrets (see the `kubernetes/` ConfigMap/Secret notes).
- Validate config at startup with `@Validated` + constraints on the properties class:

```java
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotBlank private String secret;        // app won't start if missing
    @Positive private long accessTtlSeconds;
}
```
This project uses `JwtProperties` and `OtpProperties` exactly this way.

---

## 📌 Property resolution order (highest wins, simplified)

1. Command-line args (`--server.port=9000`)
2. Environment variables / JVM system properties
3. `application-{profile}.yml`
4. `application.yml`
5. Defaults in code

➡️ Next: **[12 – Spring AI](./12-spring-ai.md)**

