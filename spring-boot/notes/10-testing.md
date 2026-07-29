# 10 · Testing

> Spring Boot supports **unit tests** (fast, isolated) and **integration/slice tests**
> (load part or all of the Spring context). JUnit 5 + Mockito + Spring Test.

---

## 🧪 The testing pyramid

```
        ▲  few    End-to-end / @SpringBootTest   (slow, full context)
       ▲▲▲        Slice tests  @WebMvcTest, @DataJpaTest  (medium)
      ▲▲▲▲▲ many  Unit tests   plain JUnit + Mockito       (fast, no Spring)
```
Write **many** fast unit tests, **fewer** slow full-context tests.

---

## 🔹 Unit test (no Spring) — fastest

Test one class, mock its collaborators with Mockito:

```java
@ExtendWith(MockitoExtension.class)          // enable Mockito
class OtpGeneratorTest {

    @Mock  Clock clock;                        // a fake dependency
    @InjectMocks OtpGenerator generator;       // inject mocks into the class under test

    @Test
    @DisplayName("generates a 6-digit code")
    void generatesSixDigits() {
        String code = generator.generate();
        assertThat(code).hasSize(6).containsOnlyDigits();
    }
}
```

| Annotation | Role |
|-----------|------|
| `@ExtendWith(MockitoExtension.class)` | wires Mockito into JUnit 5 |
| `@Mock` | creates a mock object |
| `@InjectMocks` | builds the real object and injects the mocks |
| `@Test` | marks a test method |
| `@DisplayName` | readable test name in reports |
| `@BeforeEach` | setup run before each test |

---

## 🔹 Parameterized tests — one test, many inputs

```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "12345", "1234567"})
@DisplayName("rejects invalid OTP lengths")
void rejectsBadLengths(String bad) {
    assertThat(validator.isValid(bad)).isFalse();
}
```

---

## 🔹 Slice test — web layer only (`@WebMvcTest`)

Loads just the controller + MVC infrastructure (no DB, no services). Mock the service:

```java
@WebMvcTest(ProductController.class)          // only this controller's web slice
class ProductControllerTest {

    @Autowired MockMvc mockMvc;                // simulate HTTP without a server
    @MockitoBean ProductService service;       // replace the real bean with a mock

    @Test
    void returns404WhenMissing() throws Exception {
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("nope"));

        mockMvc.perform(get("/api/products/99"))
               .andExpect(status().isNotFound());
    }
}
```

| Annotation | Role |
|-----------|------|
| `@WebMvcTest(X.class)` | loads only the web slice for controller `X` |
| `MockMvc` | calls endpoints in-process (no real Tomcat) |
| `@MockitoBean` (new) / `@MockBean` (older) | puts a Mockito mock **into the Spring context** |

---

## 🔹 Full integration test (`@SpringBootTest`)

Loads the **entire** application context — closest to production:

```java
@SpringBootTest                               // full context
@AutoConfigureMockMvc                          // provide a MockMvc bean
class AuthFlowIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("full OTP → JWT happy path")
    void otpToJwt() throws Exception {
        // 1. request OTP
        mockMvc.perform(post("/auth/otp/request")
                .contentType(APPLICATION_JSON)
                .content("{\"phone\":\"+919000000000\"}"))
               .andExpect(status().isOk());
        // 2. verify OTP → expect tokens ...
    }
}
```

| Annotation | Role |
|-----------|------|
| `@SpringBootTest` | boots the full context (can start a real server with `webEnvironment`) |
| `@AutoConfigureMockMvc` | adds a `MockMvc` so you can hit endpoints without a live port |

---

## 🎭 `@Mock` vs `@MockBean`/`@MockitoBean`

| | `@Mock` (Mockito) | `@MockBean` / `@MockitoBean` (Spring) |
|---|-------------------|----------------------------------------|
| Needs Spring context? | ❌ no | ✅ yes |
| What it does | plain mock object | mock **registered in the container**, replacing the real bean |
| Use in | unit tests | slice / integration tests |

> `@MockitoBean` is the newer Spring Boot 3.4+ name; `@MockBean` is the older (now deprecated) one.

---

## 🧰 AssertJ + Mockito quick reference

```java
// AssertJ — fluent assertions
assertThat(result).isNotNull();
assertThat(list).hasSize(3).contains("a");
assertThatThrownBy(() -> svc.get(0L)).isInstanceOf(ResourceNotFoundException.class);

// Mockito — stub & verify
when(repo.findById(1L)).thenReturn(Optional.of(product));
verify(repo, times(1)).save(any(Product.class));
```

---

## ✅ Best practices

- **Name tests behaviourally** with `@DisplayName` ("returns 404 when product missing").
- **Arrange–Act–Assert** structure.
- Prefer **unit tests**; reserve `@SpringBootTest` for real end-to-end flows (they're slow).
- Use `@DataJpaTest` for repository tests (loads only the JPA slice + in-memory DB).
- One logical assertion per test where practical.

➡️ Next: **[11 – Configuration & Properties](./11-configuration-properties.md)**

