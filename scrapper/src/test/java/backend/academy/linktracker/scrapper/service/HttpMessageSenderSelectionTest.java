package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "app.message-transport.type=HTTP")
class HttpMessageSenderSelectionTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void httpTransport_shouldLoadHttpMessageSender() {
        MessageSender sender = context.getBean(MessageSender.class);
        assertThat(sender).isInstanceOf(HttpMessageSender.class);
    }

    @Test
    void httpTransport_shouldNotLoadKafkaMessageSender() {
        assertThatThrownBy(() -> context.getBean(KafkaMessageSender.class))
                .isInstanceOf(NoSuchBeanDefinitionException.class);
    }
}
