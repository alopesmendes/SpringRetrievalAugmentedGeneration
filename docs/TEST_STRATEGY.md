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

### Test Methods
- Follow `given ... when ... then ...` pattern using Kotlin backtick syntax
- Use spaces for readability

```kotlin
@Test
fun `given valid image when analyze then returns analysis result`() { }

@Test
fun `given invalid user id when get user then throws NotFoundException`() { }

@Test
fun `given existing image when delete then image is removed`() { }
```

### Test Files
- Unit tests: `{ClassName}Test.kt`
- Integration tests: `{ClassName}IntegrationTest.kt`
- E2E tests: `{Feature}E2ETest.kt`

Examples:
- `AnalyzeImageUseCaseTest.kt`
- `MongoImageRepositoryIntegrationTest.kt`
- `ImageAnalysisE2ETest.kt`

---

## Testing by Layer

### Domain Layer (Unit Tests)
- Test business logic and validation rules
- Test value object creation and constraints
- Test domain service behavior
- **No mocking** - pure functions and logic

```kotlin
class ImageTest {
    @Test
    fun `given invalid url when create image then throws InvalidImageException`() { }
    
    @Test
    fun `given valid metadata when create image then image is created`() { }
}
```

### Application Layer (Unit Tests)
- Test use case orchestration
- Mock output ports (repositories, external services)
- Verify correct port interactions

```kotlin
class AnalyzeImageUseCaseTest {
    @Mock
    private lateinit var imageRepository: IImageRepository
    
    @Mock
    private lateinit var aiAnalyzer: IAIImageAnalyzer
    
    @Test
    fun `given valid command when invoke then image is saved with analysis`() { }
}
```

### Infrastructure Layer (Integration Tests)
- Test adapters with real dependencies
- Use test containers for databases
- Test REST controllers with MockMvc or WebTestClient

```kotlin
class MongoImageRepositoryIntegrationTest {
    @Test
    fun `given image when save then image is persisted in mongo`() { }
}

class ImageControllerIntegrationTest {
    @Test
    fun `given valid request when post image then returns 201 created`() { }
}
```

### E2E Tests
- Test complete user workflows
- Run against full application
- Verify end-to-end behavior

```kotlin
class ImageAnalysisE2ETest {
    @Test
    fun `given authenticated user when upload and analyze image then returns analysis result`() { }
}
```

---

## Mocking Strategy

### When to Mock
- Output ports in application layer tests
- External services in integration tests
- **Never mock** domain objects

### What to Mock

| Layer          | Mock                        | Don't Mock              |
|----------------|-----------------------------|-------------------------|
| Domain         | Nothing                     | Everything              |
| Application    | Repositories, external ports| Domain entities, VOs    |
| Infrastructure | External APIs (optional)    | Framework components    |

---

## Test Organization

Mirror the main source structure:

```
src/
└── test/
    └── kotlin/
        └── com/example/projectname/
            ├── domain/
            │   ├── model/
            │   │   ├── entity/
            │   │   └── valueobject/
            │   └── service/
            ├── application/
            │   └── usecase/
            └── infrastructure/
                ├── adapter/
                │   ├── inbound/
                │   │   └── rest/
                │   └── outbound/
                │       ├── persistence/
                │       └── ai/
                └── e2e/
```
