package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BotUpdatesController {
    private final NotificationService notificationService;

    @PostMapping("/updates")
    public ResponseEntity<Void> sendUpdate(@Valid @RequestBody LinkUpdate linkUpdate) {
        notificationService.sendUpdate(linkUpdate);
        return ResponseEntity.ok().build();
    }
}
