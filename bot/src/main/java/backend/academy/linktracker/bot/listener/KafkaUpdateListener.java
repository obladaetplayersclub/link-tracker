package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport.type", havingValue = "KAFKA", matchIfMissing = true)
public class KafkaUpdateListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.group-id}")
    public void onUpdate(LinkUpdate linkUpdate) {
        log.atInfo()
                .addKeyValue("link_id", linkUpdate.id())
                .addKeyValue("link_url", linkUpdate.url())
                .addKeyValue("chat_ids_count", linkUpdate.tgChatIds().size())
                .log("Получено обновление из Kafka");
        notificationService.sendUpdate(linkUpdate);
    }
}
