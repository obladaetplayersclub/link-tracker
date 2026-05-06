package backend.academy.linktracker.bot.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.kafka.KafkaContainersConfiguration;
import backend.academy.linktracker.bot.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "app.message-transport.type=KAFKA",
            "app.kafka.topic=link-updates-dlq-test",
            "app.kafka.dlq-topic=link-updates-dlq-test.DLQ",
            "app.kafka.group-id=test-bot-dlq-group",
            "app.kafka.max-retries=2",
            "app.kafka.retry-backoff-ms=100"
        })
class KafkaUpdateListenerDlqTest extends KafkaContainersConfiguration {

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void deserializationError_shouldSendToDlq_withoutRetry() throws Exception {
        sendRawMessage("link-updates-dlq-test", 1L, "{not a json}".getBytes());

        try (KafkaConsumer<Long, String> dlqConsumer = createDlqConsumer("dlq-deser")) {
            dlqConsumer.subscribe(List.of("link-updates-dlq-test.DLQ"));
            ConsumerRecords<Long, String> records = pollUntilNotEmpty(dlqConsumer);
            assertThat(records.count()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void validationError_shouldSendToDlq_withoutRetry() throws Exception {
        LinkUpdate invalid = new LinkUpdate(1L, URI.create("https://github.com/test"), "desc", List.of());
        sendJson("link-updates-dlq-test", invalid);

        try (KafkaConsumer<Long, String> dlqConsumer = createDlqConsumer("dlq-valid")) {
            dlqConsumer.subscribe(List.of("link-updates-dlq-test.DLQ"));
            ConsumerRecords<Long, String> records = pollUntilNotEmpty(dlqConsumer);
            assertThat(records.count()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void businessError_shouldRetry_andEventuallyEndUpInDlq() throws Exception {
        doThrow(new RuntimeException("Telegram down")).when(notificationService).sendUpdate(any(LinkUpdate.class));

        LinkUpdate valid = new LinkUpdate(1L, URI.create("https://github.com/test"), "desc", List.of(123L));
        sendJson("link-updates-dlq-test", valid);

        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> verify(notificationService, atLeast(3)).sendUpdate(any(LinkUpdate.class)));

        try (KafkaConsumer<Long, String> dlqConsumer = createDlqConsumer("dlq-biz")) {
            dlqConsumer.subscribe(List.of("link-updates-dlq-test.DLQ"));
            ConsumerRecords<Long, String> records = pollUntilNotEmpty(dlqConsumer);
            assertThat(records.count()).isGreaterThanOrEqualTo(1);
        }

        verify(telegramBot, org.mockito.Mockito.never()).execute(any(SendMessage.class));
    }

    private void sendJson(String topic, LinkUpdate update) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        sendRawMessage(topic, update.id(), mapper.writeValueAsBytes(update));
    }

    private void sendRawMessage(String topic, Long key, byte[] payload) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArraySerializer");

        try (KafkaProducer<Long, byte[]> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, key, payload)).get();
        }
    }

    private KafkaConsumer<Long, String> createDlqConsumer(String groupSuffix) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-test-consumer-" + groupSuffix + "-" + System.nanoTime());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    private ConsumerRecords<Long, String> pollUntilNotEmpty(KafkaConsumer<Long, String> consumer) {
        long deadline = System.currentTimeMillis() + 20_000;
        ConsumerRecords<Long, String> records = ConsumerRecords.empty();
        while (System.currentTimeMillis() < deadline) {
            records = consumer.poll(Duration.ofSeconds(2));
            if (!records.isEmpty()) return records;
        }
        return records;
    }
}
