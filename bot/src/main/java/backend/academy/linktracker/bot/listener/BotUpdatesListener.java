package backend.academy.linktracker.bot.listener;

import backend.academy.linktracker.bot.service.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class BotUpdatesListener implements UpdatesListener {
    private final TelegramBot telegramBot;
    private final CommandDispatcher commandDispatcher;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                if (update.message() != null) {
                    Long chatId = update.message().chat().id();
                    MDC.put("chat_id", String.valueOf(chatId));
                    MDC.put("update_id", String.valueOf(update.updateId()));
                    if (update.message().text() != null) {
                        MDC.put("command", update.message().text().split(" ")[0]);
                    }
                    log.atInfo().log("Обработка входящего обновления");
                }
                SendMessage response = commandDispatcher.process(update);
                if (response != null) {
                    telegramBot.execute(response);
                    log.atInfo().log("Ответ отправлен успешно");
                }
            } catch (Exception e) {
                log.error("Ошибка ", e);
            } finally {
                MDC.clear();
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }
}
