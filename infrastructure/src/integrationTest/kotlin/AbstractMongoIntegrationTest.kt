import com.ailtontech.ImageRetrievalAugmentedGeneration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

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
