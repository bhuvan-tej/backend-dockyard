# 09 · Security & JWT

> Based on `03-otp-jwt-auth` — a passwordless **OTP → JWT** flow with refresh-token rotation.

---

## 🗺️ The big picture

```
1. Request OTP        POST /auth/otp/request   { phone }        → creates OtpChallenge, "sends" code
2. Verify OTP         POST /auth/otp/verify     { phone, code } → returns Access + Refresh JWT
3. Call secured API   GET  /me   Authorization: Bearer <access> → filter validates token
4. Refresh           POST /auth/refresh        { refreshToken } → NEW access + NEW refresh (rotation)
```

---

## 🔑 Access token vs Refresh token

| | Access token | Refresh token |
|---|-------------|---------------|
| Lifetime | short (e.g. 15 min) | long (e.g. 7 days) |
| Used for | every API call | only to get a new access token |
| Stored | client memory | DB + client (so it can be revoked) |
| If stolen | limited window | dangerous → that's why we **rotate** |

**Rotation** = each refresh returns a *new* refresh token and invalidates the old one.
Reusing an old (already-rotated) refresh token signals theft → revoke the whole chain.

---

## 🔐 JWT anatomy

A JWT is three Base64 parts: `header.payload.signature`.

```
eyJhbGciOiJIUzI1NiJ9        ← header  { "alg": "HS256" }
.eyJzdWIiOiIrOTE5..."         ← payload { "sub": "userId", "exp": ..., "type": "ACCESS" }
.4pQ...signature              ← HMAC signature over header+payload with a secret key
```

- **Stateless** — the server doesn't store access tokens; it just verifies the signature.
- The `type` claim distinguishes **ACCESS** vs **REFRESH** (see `TokenType` enum).
- ⚠️ The payload is **signed, not encrypted** — never put secrets in it.

`JwtService` creates & parses tokens (`generate`, `parse`, validates `exp` and signature).

---

## 🧱 The security wiring

### `SecurityConfig` — the filter chain

```java
@Configuration
@EnableWebSecurity                           // turn on Spring Security
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RestAuthenticationEntryPoint entryPoint;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                    // stateless API → no CSRF
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS)) // no server sessions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/swagger-ui/**").permitAll() // public
                .anyRequest().authenticated())                // everything else needs a token
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint)) // 401 as JSON
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

| Piece | Why |
|-------|-----|
| `@EnableWebSecurity` | activates Spring Security's configuration |
| `SecurityFilterChain` bean | modern (no `WebSecurityConfigurerAdapter`) way to configure rules |
| `csrf().disable()` | CSRF protection is for cookie sessions; a Bearer-token API doesn't need it |
| `STATELESS` | no `HttpSession`; auth comes from the token each request |
| `permitAll()` / `authenticated()` | which paths are public vs protected |
| `addFilterBefore(jwtFilter, ...)` | run our JWT check before the default auth filter |

### `JwtAuthenticationFilter` — runs on every request

Extends `OncePerRequestFilter`:

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ... {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            var parsed = jwtService.parse(token);            // validate signature + exp
            if (parsed.isValid() && parsed.type() == ACCESS) {
                var auth = new UsernamePasswordAuthenticationToken(
                        parsed.subject(), null, List.of());  // authorities/roles here
                SecurityContextHolder.getContext().setAuthentication(auth); // mark as logged in
            }
        }
        chain.doFilter(req, res);                            // continue
    }
}
```
Once the `SecurityContext` holds an `Authentication`, the request reaches secured endpoints.

### `RestAuthenticationEntryPoint`
Returns a clean **401 JSON** body instead of the default HTML login redirect when auth is
missing/invalid.

---

## 🔢 OTP mechanics

| Component | Role |
|-----------|------|
| `OtpGenerator` | creates a random 6-digit code |
| `OtpChallenge` (entity) | stores the challenge: phone, **hashed** code, expiry, attempt count |
| `OtpService` | request/verify logic: rate-limit, expiry, attempt cap |
| `OtpProperties` | tunables (length, TTL, max attempts) via `@ConfigurationProperties` |

**Security details worth mentioning in interviews:**
- Store the **hash** of the OTP, not the plaintext (`BCrypt`/`PasswordEncoder`).
- Enforce **expiry** (`OtpExpiredException`) and a **max attempts** cap
  (`TooManyAttemptsException`) to stop brute force.
- One-time use — invalidate the challenge after a successful verify.

---

## 🔄 Refresh rotation flow

```
client → POST /auth/refresh { refreshToken }
   │
   ├─ look up token in RefreshTokenRepository
   ├─ if missing/expired/already-used → 401 (InvalidTokenException)
   ├─ mark old token used  (rotation)
   ├─ issue NEW access + NEW refresh (TokenIssuer / TokenService)
   └─ return both
```

Storing refresh tokens in the DB is what lets you **revoke** them (logout / theft detection),
even though access tokens stay stateless.

---

## 🧯 Security-specific exceptions (mapped in `GlobalExceptionHandler`)

| Exception | Status |
|-----------|--------|
| `InvalidOtpException` | 401 |
| `OtpExpiredException` | 410 / 401 |
| `TooManyAttemptsException` | 429 Too Many Requests |
| `ChallengeNotFoundException` | 404 |
| `InvalidTokenException` | 401 |

---

## ✅ Interview soundbites

- "Access tokens are **stateless & short-lived**; refresh tokens are **stored & rotated**."
- "CSRF is disabled because we authenticate with a **Bearer header**, not cookies."
- "OTPs are **hashed at rest**, **expire**, and are **attempt-limited**."
- "`SessionCreationPolicy.STATELESS` means each request re-authenticates from the token."

➡️ Next: **[10 – Testing](./10-testing.md)**

