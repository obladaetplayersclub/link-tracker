package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.kafka.KafkaContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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
class KafkaMessageSenderSelectionTest extends KafkaContainersConfiguration {

    @Autowired
    private ApplicationContext context;

    @Test
    void kafkaTransport_shouldLoadKafkaMessageSender() {
        MessageSender sender = context.getBean(MessageSender.class);
        assertThat(sender).isInstanceOf(KafkaMessageSender.class);
    }

    @Test
    void kafkaTransport_shouldNotLoadHttpMessageSender() {
        assertThatThrownBy(() -> context.getBean(HttpMessageSender.class))
                .isInstanceOf(NoSuchBeanDefinitionException.class);
    }
}
