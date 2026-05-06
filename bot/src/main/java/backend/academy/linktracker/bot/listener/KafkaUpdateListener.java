package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.NotificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final Validator validator;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.group-id}")
    public void onUpdate(LinkUpdate linkUpdate) {
        Set<ConstraintViolation<LinkUpdate>> violations = validator.validate(linkUpdate);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Invalid LinkUpdate: " + message);
        }

        log.atInfo()
                .addKeyValue("link_id", linkUpdate.id())
                .addKeyValue("link_url", linkUpdate.url())
                .addKeyValue("chat_ids_count", linkUpdate.tgChatIds().size())
                .log("Получено обновление из Kafka");
        notificationService.sendUpdate(linkUpdate);
    }
}
