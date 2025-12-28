# Testing Strategy

## Test Pyramid

```
        /\
       /  \        E2E Tests (few)
      /────\       - Complete workflows
     /      \  
    /────────\     Integration Tests (some)
   /          \    - Adapters, DB, external services
  /────────────\  
 /              \  Unit Tests (many)
/────────────────\ - Domain logic, use cases
```

| Type        | Quantity | Speed  | Scope                          |
|-------------|----------|--------|--------------------------------|
| Unit        | Many     | Fast   | Single class/function          |
| Integration | Some     | Medium | Adapter + external dependency  |
| E2E         | Few      | Slow   | Full application workflow      |

---

## Naming Conventions

### Domain Layer Tests
Domain tests focus on **business rules and invariants**. Use a declarative naming style that describes the expected behavior:

```kotlin
@Test
fun `should create user with valid email`() { }

@Test
fun `should reject user with blank name`() { }

@Test
fun `should generate unique user id on creation`() { }
```

### Application Layer Tests
Application tests validate **use case orchestration**. Follow the `given ... when ... then ...` pattern using Kotlin backtick syntax:

```kotlin
@Test
fun `given valid command when register user then user is saved`() { }

@Test
fun `given existing email when register user then throws DuplicateUserException`() { }

@Test
fun `given valid credentials when authenticate then returns auth token`() { }
```

### Infrastructure Layer Tests

#### Integration Tests
Integration tests verify **adapter behavior with real dependencies**. Use camelCase with an action-outcome pattern:

```kotlin
@Test
fun saveUserPersistsDocumentInMongoDB() { }

@Test
fun findByEmailReturnsUserWhenExists() { }

@Test
fun postUsersEndpointReturns201WithValidRequest() { }
```

#### E2E Tests
E2E tests validate **complete user workflows**. Use camelCase with scenario-based naming:

```kotlin
@Test
fun userCanRegisterAndReceiveWelcomeEmail() { }

@Test
fun userCanLoginAndAccessProtectedResources() { }

@Test
fun userCanUploadImageAndRetrieveAnalysisResults() { }
```

### Test Files

| Layer          | Test Type   | Naming Pattern                  | Example                                 |
|----------------|-------------|---------------------------------|-----------------------------------------|
| Domain         | Unit        | `{ClassName}Test.kt`            | `UserTest.kt`, `EmailTest.kt`           |
| Application    | Unit        | `{UseCaseName}Test.kt`          | `RegisterUserUseCaseTest.kt`            |
| Infrastructure | Integration | `{ClassName}IntegrationTest.kt` | `MongoUserRepositoryIntegrationTest.kt` |
| Infrastructure | E2E         | `{Feature}E2ETest.kt`           | `UserRegistrationE2ETest.kt`            |

---

## Mocking Strategy

### Mocking Libraries
- **MockK** - For application layer unit tests (pure Kotlin mocking)
- **SpringMockK** - For infrastructure layer tests (Spring context integration with `@MockkBean`)

### When to Mock
- Output ports in application layer tests
- External services in integration tests
- **Never mock** domain objects

### What to Mock

| Layer          | Mock                         | Don't Mock           |
|----------------|------------------------------|----------------------|
| Domain         | Nothing                      | Everything           |
| Application    | Repositories, external ports | Domain entities, VOs |
| Infrastructure | External APIs (optional)     | Framework components |

### Mocking Guidelines

**Domain Layer**: No mocking required. Tests should exercise pure business logic with real domain objects.

**Application Layer**: Use `mockk<T>()` to create mocks for all output ports.

```kotlin
class RegisterUserUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val emailService = mockk<IEmailService>()
    private val useCase = RegisterUserUseCase(userRepository, emailService)

    @Test
    fun `given valid command when register user then user is saved`() {
        every { userRepository.save(any()) } answers { firstArg() }

        // ... test logic

        verify { userRepository.save(any()) }
        confirmVerified(userRepository)
    }
}
```

**Infrastructure Layer**: Use `@MockkBean` from SpringMockK when mocking Spring beans within the Spring context. Use real dependencies when possible (Testcontainers for databases).

```kotlin
@WebMvcTest(UserController::class)
class UserControllerIntegrationTest {
    @MockkBean
    private lateinit var userUseCase: IRegisterUserUseCase

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun postUsersReturns201() {
        every { userUseCase(any()) } returns Result.success(UserResult("123", "John"))

        // ... test logic

        verify { userUseCase(any()) }
    }
}
```

---

## Testing by Layer

### Domain Layer (Unit Tests)
Test business logic, validation rules, and domain invariants. No mocking - pure functions and logic.

```kotlin
class UserTest {
    @Test
    fun `should create user with valid data`() {
        val user = User.create(
            name = "John Doe",
            email = Email("john@example.com")
        )

        assertNotNull(user.id)
        assertEquals("John Doe", user.name)
    }

    @Test
    fun `should reject user with blank name`() {
        assertThrows<InvalidUserException> {
            User.create(name = "", email = Email("john@example.com"))
        }
    }
}

class EmailTest {
    @Test
    fun `should create email with valid format`() {
        val email = Email("user@domain.com")
        assertEquals("user@domain.com", email.value)
    }

    @Test
    fun `should reject email with invalid format`() {
        assertThrows<InvalidEmailException> {
            Email("invalid-email")
        }
    }
}
```

### Application Layer (Unit Tests)
Test use case orchestration with mocked output ports. Verify correct port interactions.

```kotlin
class RegisterUserUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val emailService = mockk<IEmailService>()
    private val useCase = RegisterUserUseCase(userRepository, emailService)

    @Test
    fun `given valid command when register user then user is saved`() {
        val command = RegisterUserCommand(name = "John", email = "john@example.com")
        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.save(any()) } answers { firstArg() }
        every { emailService.sendWelcomeEmail(any()) } just Runs

        val result = useCase(command)

        assertTrue(result.isSuccess)
        verify { userRepository.save(any()) }
        confirmVerified(userRepository)
    }

    @Test
    fun `given existing email when register user then throws DuplicateUserException`() {
        val command = RegisterUserCommand(name = "John", email = "existing@example.com")
        every { userRepository.existsByEmail(any()) } returns true

        val result = useCase(command)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DuplicateUserException)
    }

    @Test
    fun `given successful registration when register user then welcome email is sent`() {
        val command = RegisterUserCommand(name = "John", email = "john@example.com")
        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.save(any()) } answers { firstArg() }
        every { emailService.sendWelcomeEmail(any()) } just Runs

        useCase(command)

        verify { emailService.sendWelcomeEmail(any()) }
        confirmVerified(emailService)
    }
}
```

### Infrastructure Layer (Integration Tests)
Test adapters with real dependencies using Testcontainers for databases. Use `@MockkBean` from SpringMockK when mocking Spring beans.

```kotlin
@Testcontainers
class MongoUserRepositoryIntegrationTest {

    companion object {
        @Container
        val mongoContainer = MongoDBContainer("mongo:8")
    }

    @Autowired
    private lateinit var repository: MongoUserRepository

    @Test
    fun saveUserPersistsDocumentInMongoDB() {
        val user = User.create(name = "John", email = Email("john@example.com"))

        val saved = repository.save(user)

        assertNotNull(saved.id)
        assertEquals(user.name, saved.name)
    }

    @Test
    fun findByEmailReturnsUserWhenExists() {
        val user = User.create(name = "Jane", email = Email("jane@example.com"))
        repository.save(user)

        val found = repository.findByEmail(Email("jane@example.com"))

        assertNotNull(found)
        assertEquals("Jane", found?.name)
    }

    @Test
    fun findByEmailReturnsNullWhenNotExists() {
        val found = repository.findByEmail(Email("unknown@example.com"))

        assertNull(found)
    }
}

@WebMvcTest(UserController::class)
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var registerUserUseCase: IRegisterUserUseCase

    @Test
    fun postUsersEndpointReturns201WithValidRequest() {
        every { registerUserUseCase(any()) } returns Result.success(UserResult(id = "123", name = "John"))

        val request = """{"name": "John", "email": "john@example.com"}"""

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())

        verify { registerUserUseCase(any()) }
    }

    @Test
    fun postUsersEndpointReturns400WithInvalidEmail() {
        val request = """{"name": "John", "email": "invalid"}"""

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isBadRequest)
    }
}
```

### E2E Tests
Test complete user workflows against the full application.

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserRegistrationE2ETest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun userCanRegisterAndLoginSuccessfully() {
        // Register
        val registerRequest = RegisterRequest(name = "John", email = "john@example.com", password = "secret123")
        val registerResponse = restTemplate.postForEntity(
            "http://localhost:$port/api/users",
            registerRequest,
            UserResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)

        // Login
        val loginRequest = LoginRequest(email = "john@example.com", password = "secret123")
        val loginResponse = restTemplate.postForEntity(
            "http://localhost:$port/api/auth/login",
            loginRequest,
            AuthResponse::class.java
        )
        assertEquals(HttpStatus.OK, loginResponse.statusCode)
        assertNotNull(loginResponse.body?.token)
    }

    @Test
    fun userCanAccessProtectedResourceAfterAuthentication() {
        // Register and login first
        val token = registerAndLogin("test@example.com", "password123")

        // Access protected resource
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }
        val response = restTemplate.exchange(
            "http://localhost:$port/api/users/me",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            UserResponse::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
```

---

## Test Organization

Tests are organized within each module following hexagonal architecture boundaries:

```
project/
├── domain/
│   └── src/
│       ├── main/kotlin/com/ailtontech/imagerag/domain/
│       └── test/kotlin/com/ailtontech/imagerag/domain/
│           ├── model/
│           │   ├── entity/
│           │   │   └── UserTest.kt
│           │   └── valueobject/
│           │       └── EmailTest.kt
│           └── service/
│               └── UserDomainServiceTest.kt
│
├── application/
│   └── src/
│       ├── main/kotlin/com/ailtontech/imagerag/application/
│       └── test/kotlin/com/ailtontech/imagerag/application/
│           └── usecase/
│               ├── RegisterUserUseCaseTest.kt
│               └── AuthenticateUserUseCaseTest.kt
│
└── infrastructure/
    └── src/
        ├── main/kotlin/com/ailtontech/imagerag/infrastructure/
        │
        ├── integrationTest/kotlin/com/ailtontech/imagerag/infrastructure/
        │   └── adapter/
        │       ├── primary/rest/
        │       │   └── UserControllerIntegrationTest.kt
        │       └── secondary/
        │           ├── persistence/
        │           │   └── MongoUserRepositoryIntegrationTest.kt
        │           └── ai/
        │               └── OpenAIImageAnalyzerIntegrationTest.kt
        │
        └── e2eTest/kotlin/com/ailtontech/imagerag/infrastructure/
            └── e2e/
                ├── UserRegistrationE2ETest.kt
                └── ImageAnalysisE2ETest.kt
```

### Gradle Configuration (Infrastructure Module)

Configure separate source sets for integration and E2E tests in `infrastructure/build.gradle.kts`:

```kotlin
// Source Sets
val integrationTest: SourceSet by sourceSets.creating {
    kotlin.srcDir("src/integrationTest/kotlin")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}

val e2eTest: SourceSet by sourceSets.creating {
    kotlin.srcDir("src/e2eTest/kotlin")
    resources.srcDir("src/e2eTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
}

// Extend test configurations
configurations {
    named("integrationTestImplementation") { extendsFrom(configurations.testImplementation.get()) }
    named("integrationTestRuntimeOnly") { extendsFrom(configurations.testRuntimeOnly.get()) }
    named("e2eTestImplementation") { extendsFrom(configurations.testImplementation.get()) }
    named("e2eTestRuntimeOnly") { extendsFrom(configurations.testRuntimeOnly.get()) }
}

// Test Tasks
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("e2eTest") {
    description = "Runs E2E tests with Cucumber"
    group = "verification"
    testClassesDirs = sourceSets["e2eTest"].output.classesDirs
    classpath = sourceSets["e2eTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.named("integrationTest"))
}
```
