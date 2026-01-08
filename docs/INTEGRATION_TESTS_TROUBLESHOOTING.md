# Integration Test Troubleshooting Guide

## Error: "Could not find a valid Docker environment"

This error means Testcontainers cannot connect to Docker. Here are the solutions:

### 1. Verify Docker is Running

```bash
# Check if Docker daemon is running
docker info

# If not running, start Docker
# macOS/Windows: Start Docker Desktop
# Linux: sudo systemctl start docker
```

### 2. Check Docker Socket Permissions (Linux)

```bash
# Add your user to the docker group
sudo usermod -aG docker $USER

# Apply the new group membership (or log out and back in)
newgrp docker

# Verify
docker ps
```

### 3. Set Docker Host (if using remote Docker or Docker Machine)

```bash
# In your environment or build.gradle.kts
export DOCKER_HOST=tcp://localhost:2375

# Or in build.gradle.kts for tests
tasks.withType<Test> {
    environment("DOCKER_HOST", "tcp://localhost:2375")
}
```

### 4. For WSL2 (Windows Subsystem for Linux)

Ensure Docker Desktop has WSL2 integration enabled:
1. Open Docker Desktop Settings
2. Resources → WSL Integration
3. Enable for your WSL distro

### 5. Testcontainers Configuration File

Create `~/.testcontainers.properties`:

```properties
# Use Docker socket
docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy

# Or for TCP
# docker.host=tcp://localhost:2375
```

---

## Error: "Unable to find a @SpringBootConfiguration"

This occurs when your test package doesn't match your main application package.

### Root Cause

Spring Boot searches **upward** from the test package to find `@SpringBootConfiguration`:

```
Test:  user.adapter.secondary.persistence.UserRepositoryIntegrationTest
       ↑ (searches upward)
       user.adapter.secondary.persistence
       user.adapter.secondary
       user.adapter
       user
       (root) ← No @SpringBootConfiguration found!

App:   com.ailtontech.ImageRetrievalAugmentedGeneration
       ↑ (not in the search path!)
```

### Solution 1: Match Package Structure (Recommended)

Move tests to match your main app's package:

```
Before: user.adapter.secondary.persistence.UserRepositoryIntegrationTest
After:  com.ailtontech.imagerag.infrastructure.adapter.secondary.persistence.UserRepositoryIntegrationTest
```

### Solution 2: Explicit Configuration

If you can't change packages, use `@ContextConfiguration`:

```kotlin
@Testcontainers
@DataMongoTest
@ContextConfiguration(classes = [ImageRetrievalAugmentedGeneration::class])
abstract class AbstractBaseIntegrationTest
```

---

## Common Mistakes to Avoid

| Mistake                                          | Correct Approach                                                                |
|--------------------------------------------------|---------------------------------------------------------------------------------|
| `excludeAutoConfiguration = [MyApp::class]`      | Use `properties = ["spring.autoconfigure.exclude=..."]` for auto-config classes |
| Missing `@JvmStatic` on companion object methods | Always add `@JvmStatic` for `@DynamicPropertySource` and `@Container`           |
| Starting container manually                      | Let `@Container` annotation handle lifecycle                                    |
| No `@ActiveProfiles("test")`                     | Add it to use `application-test.yml`                                            |

---

## Correct Abstract Test Class Template

```kotlin
@Testcontainers
@DataMongoTest
@ActiveProfiles("test")
@ContextConfiguration(classes = [ImageRetrievalAugmentedGeneration::class])
abstract class AbstractMongoIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val mongoDBContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:8.0"))
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

---

## Running Tests

```bash
# Run only integration tests
./gradlew :infrastructure:integrationTest

# With verbose output
./gradlew :infrastructure:integrationTest --info

# Skip Testcontainers (CI without Docker)
./gradlew :infrastructure:integrationTest -DskipTestcontainers=true
```
