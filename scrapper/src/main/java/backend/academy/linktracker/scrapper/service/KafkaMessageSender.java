package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.events.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport.type", havingValue = "KAFKA", matchIfMissing = true)
public class KafkaMessageSender implements MessageSender {

    private final KafkaTemplate<Long, LinkUpdateEvent> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Override
    public void send(LinkUpdate linkUpdate) {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(linkUpdate.id())
                .setUrl(linkUpdate.url().toString())
                .setDescription(linkUpdate.description())
                .setTgChatIds(linkUpdate.tgChatIds())
                .build();

        log.info("Sending update for link {} to Kafka topic {}", linkUpdate.id(), kafkaProperties.getTopic());
        kafkaTemplate.send(kafkaProperties.getTopic(), linkUpdate.id(), event);
    }
}
