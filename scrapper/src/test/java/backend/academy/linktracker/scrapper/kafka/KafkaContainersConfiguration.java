package backend.academy.linktracker.scrapper.kafka;

import backend.academy.linktracker.scrapper.repository.ContainersConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class KafkaContainersConfiguration extends ContainersConfiguration {

    private static final Network NETWORK = Network.newNetwork();

    protected static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.7.1"))
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka");

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> SCHEMA_REGISTRY = new GenericContainer<>(
                    DockerImageName.parse("confluentinc/cp-schema-registry:7.7.1"))
            .withNetwork(NETWORK)
            .withNetworkAliases("schema-registry")
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9093")
            .dependsOn(KAFKA);

    static {
        KAFKA.start();
        SCHEMA_REGISTRY.start();
    }

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add(
                "app.kafka.schema-registry-url",
                () -> "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getMappedPort(8081));
    }
}
