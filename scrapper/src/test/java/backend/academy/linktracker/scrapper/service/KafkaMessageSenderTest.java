package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.events.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.kafka.KafkaContainersConfiguration;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "app.message-transport.type=KAFKA",
            "app.kafka.topic=link-updates-test",
            "app.kafka.partitions=1",
            "app.kafka.replication-factor=1"
        })
class KafkaMessageSenderTest extends KafkaContainersConfiguration {

    @Autowired
    private MessageSender messageSender;

    @Test
    void happyPath_shouldSerializeAndSendLinkUpdate_toKafkaTopic() {
        LinkUpdate update = new LinkUpdate(
                42L, URI.create("https://github.com/test/repo"), "Test description", List.of(100L, 200L));

        messageSender.send(update);

        try (KafkaConsumer<Long, LinkUpdateEvent> consumer = createAvroConsumer()) {
            consumer.subscribe(List.of("link-updates-test"));
            ConsumerRecords<Long, LinkUpdateEvent> records = consumer.poll(Duration.ofSeconds(15));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<Long, LinkUpdateEvent> record = records.iterator().next();
            assertThat(record.key()).isEqualTo(42L);

            LinkUpdateEvent event = record.value();
            assertThat(event.getId()).isEqualTo(42L);
            assertThat(event.getUrl()).isEqualTo("https://github.com/test/repo");
            assertThat(event.getDescription()).isEqualTo("Test description");
            assertThat(event.getTgChatIds()).containsExactly(100L, 200L);
        }
    }

    private KafkaConsumer<Long, LinkUpdateEvent> createAvroConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.nanoTime());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(
                KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getMappedPort(8081));
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new KafkaConsumer<>(props);
    }
}
