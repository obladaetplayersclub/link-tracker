package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TelegramBot telegramBot;

    public void sendUpdate(LinkUpdate linkUpdate) {
        String text = "Обновление по ссылке: " + linkUpdate.url();
        for (Long chatId : linkUpdate.tgChatIds()) {
            telegramBot.execute(new SendMessage(chatId, text));
        }
    }
}
