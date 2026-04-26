---
name: spring-testcontainers
description: Testcontainers integration test patterns for this project. Covers AbstractMongoIntegrationTest base class, @DataMongoTest slice, @DynamicPropertySource, withReuse, test profiles, and how to add integration tests for new adapters. Trigger when writing or reviewing integration tests, AbstractMongoIntegrationTest, @DataMongoTest, Testcontainers setup, or adding a new container type.
---

# Spring Testcontainers Skill

Integration tests in `infrastructure/src/integrationTest/kotlin/`. Needs Docker. Run via `./gradlew integrationTest`.

## Base class — `AbstractMongoIntegrationTest`

```kotlin
@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
@ContextConfiguration(classes = [ImageRetrievalAugmentedGeneration::class])
abstract class AbstractMongoIntegrationTest {
    companion object {
        private const val MONGO_IMAGE = "mongo:8.0"

        @Container
        @JvmStatic
        val mongoDBContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_IMAGE))
                .withExposedPorts(27017)
                .withReuse(true)

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
        }
    }
}
```

- `@DataMongoTest` — loads only Mongo slice. No full Spring context.
- `withReuse(true)` — container reused across test classes (Testcontainers reuse must be enabled in `~/.testcontainers.properties`).
- `@DynamicPropertySource` — injects container URI at runtime.
- `@ActiveProfiles("test")` — activates `application-test.yml` if needed.

## Writing a persistence integration test

```kotlin
class UserRepositoryIntegrationTest : AbstractMongoIntegrationTest() {
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var mongoUserRepository: MongoUserRepository

    @BeforeEach
    fun setup() { mongoUserRepository.deleteAll() }

    @Test
    fun `given valid user when save then persisted and retrievable`() {
        val user = User.create(Email("a@b.com"), Name("A", "B"), PasswordHash("hash"), Age(25))
        userRepository.save(user)
        val found = userRepository.findByEmail(Email("a@b.com"))
        assertNotNull(found)
        assertEquals("a@b.com", found!!.email.value)
    }
}
```

Rules:
- Extend `AbstractMongoIntegrationTest`.
- Clean collection `@BeforeEach` — never share state between tests.
- Test the **port contract** not Spring Data internals.
- File: `{Entity}RepositoryIntegrationTest.kt` in `integrationTest/kotlin/{feature}/adapter/secondary/persistence/`.

## Full context integration tests (controllers)

For REST controller tests use `@SpringBootTest` + `MockMvc` or `WebTestClient`:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class UserControllerIntegrationTest : AbstractMongoIntegrationTest() {
    @Autowired lateinit var webTestClient: WebTestClient
    // or MockMvc

    @Test
    fun `given valid request when POST users then 201 created`() {
        webTestClient.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"email":"a@b.com","firstName":"A","lastName":"B","password":"pass","age":25}""")
            .exchange()
            .expectStatus().isCreated
    }
}
```

Note: `@DataMongoTest` already used in base class. For controller tests, consider a separate base that uses `@SpringBootTest`.

## Adding a new container type

Pattern for a Redis or other container:

```kotlin
companion object {
    @Container @JvmStatic
    val redis = GenericContainer(DockerImageName.parse("redis:7"))
        .withExposedPorts(6379)
        .withReuse(true)

    @DynamicPropertySource @JvmStatic
    fun props(registry: DynamicPropertyRegistry) {
        registry.add("spring.data.redis.host") { redis.host }
        registry.add("spring.data.redis.port") { redis.firstMappedPort }
    }
}
```

New container → new abstract base class (don't pollute `AbstractMongoIntegrationTest`). Ask user before adding.

## What to ask before writing integration test

- Test the port contract or the full HTTP flow?
- Which data to seed? Which to clean?
- Does the feature need a second container (e.g. Redis, S3)?
- Profile-specific config needed (`application-test.yml`)?
