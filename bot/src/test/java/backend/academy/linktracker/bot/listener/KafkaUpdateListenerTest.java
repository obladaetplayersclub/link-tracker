package backend.academy.linktracker.bot.listener;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.kafka.KafkaContainersConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
            "app.kafka.topic=link-updates-test",
            "app.kafka.group-id=test-bot-listener-group"
        })
class KafkaUpdateListenerTest extends KafkaContainersConfiguration {

    @MockitoBean
    private TelegramBot telegramBot;

    @Test
    void happyPath_shouldConsumeFromKafka_andSendToTelegram() throws Exception {
        LinkUpdate update =
                new LinkUpdate(1L, URI.create("https://github.com/test/test"), "Test update from Kafka", List.of(123L));

        sendToKafka("link-updates-test", update);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            verify(telegramBot, atLeastOnce()).execute(any(SendMessage.class));
        });
    }

    private void sendToKafka(String topic, LinkUpdate update) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(update);

        try (KafkaProducer<Long, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, update.id(), json)).get();
        }
    }
}
