package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport.type", havingValue = "HTTP")
public class BotUpdatesController {
    private final NotificationService notificationService;

    @PostMapping("/updates")
    public ResponseEntity<Void> sendUpdate(@Valid @RequestBody LinkUpdate linkUpdate) {
        log.atInfo()
                .addKeyValue("link_url", linkUpdate.url())
                .addKeyValue("chat_ids_count", linkUpdate.tgChatIds().size())
                .log("Получено обновление от Scrapper");
        notificationService.sendUpdate(linkUpdate);
        return ResponseEntity.ok().build();
    }
}
